/*
 * Copyright (C) 2015 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package name.christianbauer.orc3.test.chat;

import name.christianbauer.orc3.server.chat.ChatServer;
import name.christianbauer.orc3.server.chat.WebSocketChatSession;
import name.christianbauer.orc3.server.chat.WebSocketClientChatEndpoint;
import name.christianbauer.orc3.shared.chat.AbstractClientChatEndpoint;
import name.christianbauer.orc3.shared.chat.ChatClient;
import name.christianbauer.orc3.shared.chat.event.ChatConnected;
import name.christianbauer.orc3.shared.chat.event.ChatDisconnected;
import name.christianbauer.orc3.shared.chat.event.ChatError;
import name.christianbauer.orc3.shared.chat.event.ChatNickMessage;
import name.christianbauer.orc3.shared.chat.model.ChannelItem;
import org.testng.annotations.Test;

import javax.enterprise.inject.spi.CDI;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.testng.Assert.*;

public class MultiUserChatServiceTest extends WebSocketServerTest {

    private static final Logger LOG = Logger.getLogger(MultiUserChatServiceTest.class.getName());

    @Override
    protected String getServicePath() {
        return "/chat";
    }

    @Test
    public void connectNickDisconnect() throws Exception {
        CountDownLatch assertions = new CountDownLatch(3);

        final String TEST_NICK = "johndoe";

        AbstractClientChatEndpoint clientEndpoint = new WebSocketClientChatEndpoint() {

            protected String sessionId;

            protected ChatServer getServerChat() {
                return CDI.current().select(ChatServer.class).get();
            }

            @Override
            protected WebSocketContainer getWebSocketContainer() {
                return getWebSocketClientContainer();
            }

            @Override
            protected WebSocketChatSession createSession(Session session) {
                WebSocketChatSession chatSession = new WebSocketChatSession(session);

                sessionId = chatSession.getId();

                register(ChatError.class, event -> LOG.severe("Endpoint error: " + event.getReason()));
                chatSession.register(ChatError.class, event -> LOG.severe("Session error: " + event.getReason()));

                register(ChatConnected.class, event -> {
                    assertEquals(getServerChat().getChannelNames().length, 0);
                    assertFalse(getServerChat().hasNick(event.getSessionId()));
                    assertions.countDown();

                    getSession(sessionId).send(new ChatNickMessage(TEST_NICK));
                });

                chatSession.register(ChatNickMessage.class, msg -> {
                    assertEquals(msg.getNick(), TEST_NICK);
                    assertions.countDown();

                    disconnect(sessionId);
                });

                register(ChatDisconnected.class, event -> {
                    assertEquals(getServerChat().getChannelNames().length, 0);
                    assertFalse(getServerChat().hasNick(event.getSessionId()));
                    assertEquals(event.getReason(), "Chat client closing connection.");
                    assertions.countDown();
                });

                return chatSession;
            }
        };

        clientEndpoint.connect(serviceUrl);

        assertions.await(250, TimeUnit.MILLISECONDS);
        assertEquals(assertions.getCount(), 0);
    }

    @Test
    public void chatClients() throws Exception {

        class TestClient extends ChatClient {

            public TestClient() {
                super(new WebSocketClientChatEndpoint() {
                    @Override
                    protected WebSocketContainer getWebSocketContainer() {
                        return MultiUserChatServiceTest.this.getWebSocketClientContainer();
                    }

                    @Override
                    protected WebSocketChatSession createSession(Session session) {
                        WebSocketChatSession chatSession = new WebSocketChatSession(session);
                        register(ChatError.class, event -> LOG.severe("Endpoint error: " + event.getReason()));
                        chatSession.register(ChatError.class, event -> LOG.severe("Session error: " + event.getReason()));
                        return chatSession;
                    }
                });
            }
        }

        String TEST_NICK = "johndoe";
        String TEST_NICK2 = "janeroe";
        String TEST_CHANNEL = "somechannel";
        String TEST_CHANNEL2 = "otherchannel";

        TestClient first = new TestClient();
        TestClient second = new TestClient();

        first.server = serviceUrl;
        first.nick = TEST_NICK;
        second.server = serviceUrl;
        second.nick = TEST_NICK;

        first.connect();
        second.connect();
        Thread.sleep(200);
        assertNotNull(first.session);
        assertNotNull(second.session);
        assertTrue(first.sessionActive);
        assertFalse(second.sessionActive); // Nick conflict, try again with different nick
        assertNotNull(second.status);

        second.nick = TEST_NICK2;
        second.connect();
        Thread.sleep(200);
        assertTrue(second.sessionActive);

        assertEquals(first.channels.length, 0);
        assertEquals(second.channels.length, 0);
        assertNull(first.currentChannel);
        assertNull(second.currentChannel);

        first.join(TEST_CHANNEL);
        Thread.sleep(200);
        assertEquals(first.channels.length, 1);
        assertEquals(first.getChannel(TEST_CHANNEL).name, TEST_CHANNEL);
        assertEquals(first.currentChannel.name, TEST_CHANNEL);
        assertEquals(second.channels.length, 1);
        assertEquals(second.getChannel(TEST_CHANNEL).name, TEST_CHANNEL);
        assertNull(second.currentChannel);
        assertTrue(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertFalse(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertEquals(first.getNumberOfChannelMembers(TEST_CHANNEL), 1);
        assertEquals(second.getNumberOfChannelMembers(TEST_CHANNEL), 0);
        assertTrue(first.getChannel(TEST_CHANNEL).active);
        assertFalse(first.getChannel(TEST_CHANNEL).isEmpty());

        second.join(TEST_CHANNEL);
        Thread.sleep(200);
        assertEquals(second.currentChannel.name, TEST_CHANNEL);
        assertTrue(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertEquals(first.getNumberOfChannelMembers(TEST_CHANNEL), 2);
        assertEquals(second.getNumberOfChannelMembers(TEST_CHANNEL), 2);
        assertTrue(first.getChannel(TEST_CHANNEL).items[0].control);
        assertEquals(first.getChannel(TEST_CHANNEL).items[0].nick, TEST_NICK2);
        assertEquals(second.getChannel(TEST_CHANNEL).items.length, 0);

        // Reset
        first.status = null;
        second.status = null;
        first.getChannel(TEST_CHANNEL).items = new ChannelItem[0];
        second.getChannel(TEST_CHANNEL).items = new ChannelItem[0];

        second.join(TEST_CHANNEL2);
        Thread.sleep(200);
        assertEquals(second.currentChannel.name, TEST_CHANNEL2);
        assertTrue(second.getChannel(TEST_CHANNEL2).active);
        assertTrue(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertFalse(first.isMemberOfChannel(TEST_CHANNEL2, TEST_NICK2));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL2, TEST_NICK2));
        assertEquals(first.getNumberOfChannelMembers(TEST_CHANNEL), 2);
        assertEquals(first.getNumberOfChannelMembers(TEST_CHANNEL2), 0);
        assertEquals(second.getNumberOfChannelMembers(TEST_CHANNEL), 2);
        assertEquals(second.getNumberOfChannelMembers(TEST_CHANNEL2), 1);
        assertEquals(first.getChannel(TEST_CHANNEL).items.length, 0);
        assertEquals(first.getChannel(TEST_CHANNEL2).items.length, 0);

        first.sendText(TEST_CHANNEL, "Hello");
        Thread.sleep(200);
        assertEquals(first.getChannel(TEST_CHANNEL).items.length, 1);
        assertFalse(first.getChannel(TEST_CHANNEL).items[0].control);
        assertEquals(first.getChannel(TEST_CHANNEL).items[0].nick, TEST_NICK);
        assertEquals(first.getChannel(TEST_CHANNEL).items[0].text, "Hello");
        assertTrue(first.getChannel(TEST_CHANNEL).items[0].own);
        assertEquals(second.getChannel(TEST_CHANNEL).items.length, 1);
        assertFalse(second.getChannel(TEST_CHANNEL).items[0].control);
        assertEquals(second.getChannel(TEST_CHANNEL).items[0].nick, TEST_NICK);
        assertEquals(second.getChannel(TEST_CHANNEL).items[0].text, "Hello");
        assertFalse(second.getChannel(TEST_CHANNEL).items[0].own);

        // Reset
        first.status = null;
        second.status = null;
        first.getChannel(TEST_CHANNEL).items = new ChannelItem[0];
        second.getChannel(TEST_CHANNEL).items = new ChannelItem[0];

        second.part(TEST_CHANNEL);
        Thread.sleep(200);
        assertNotNull(second.status);
        assertNull(first.status);
        assertNull(second.currentChannel);
        assertEquals(first.getChannelNames().length, 2);
        assertEquals(second.getChannelNames().length, 2);
        assertTrue(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertFalse(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL2, TEST_NICK2));
        assertEquals(first.getChannel(TEST_CHANNEL).items.length, 1);
        assertTrue(first.getChannel(TEST_CHANNEL).items[0].control);
        assertEquals(first.getChannel(TEST_CHANNEL).items[0].nick, TEST_NICK2);

        // Reset
        first.status = null;
        second.status = null;
        first.getChannel(TEST_CHANNEL).items = new ChannelItem[0];
        second.getChannel(TEST_CHANNEL).items = new ChannelItem[0];

        first.disconnect();
        Thread.sleep(200);
        assertNotNull(first.status);
        assertNull(first.currentChannel);
        assertEquals(first.channels.length, 0);
        assertEquals(second.channels.length, 1);
        assertEquals(first.getChannelNames().length, 0);
        assertEquals(second.getChannelNames().length, 1);
        assertFalse(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertFalse(first.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertTrue(second.isMemberOfChannel(TEST_CHANNEL2, TEST_NICK2));

        // Reset
        first.status = null;
        second.status = null;

        second.disconnect();
        Thread.sleep(200);
        assertNotNull(second.status);
        assertNull(second.currentChannel);
        assertEquals(second.channels.length, 0);
        assertEquals(first.getChannelNames().length, 0);
        assertEquals(second.getChannelNames().length, 0);
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL, TEST_NICK2));
        assertFalse(second.isMemberOfChannel(TEST_CHANNEL2, TEST_NICK2));
    }
}
