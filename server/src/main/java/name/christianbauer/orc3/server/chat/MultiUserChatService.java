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
package name.christianbauer.orc3.server.chat;

import name.christianbauer.orc3.shared.chat.event.ChatConnected;
import name.christianbauer.orc3.shared.chat.event.ChatDisconnected;
import name.christianbauer.orc3.shared.chat.DefaultChatEndpoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.logging.Logger;

@ServerEndpoint("/chat")
@Singleton
public class MultiUserChatService extends Endpoint {

    private static final Logger LOG = Logger.getLogger(MultiUserChatService.class.getName());

    final protected ChatServer server;
    final protected DefaultChatEndpoint<MultiUserChatSession> chatEndpoint;

    @Inject
    public MultiUserChatService(ChatServer server) {
        LOG.info("New chat service endpoint: " + server.getClass().getName());
        this.server = server;
        this.chatEndpoint = new DefaultChatEndpoint<>();

        chatEndpoint.register(ChatConnected.class, event -> {
            LOG.info("Chat client session connected: " + event.getSessionId());
        });

        chatEndpoint.register(ChatDisconnected.class, event -> {
            LOG.info("Chat client session disconnected: " + event.getSessionId() + " => " + event.getReason());
            chatEndpoint.getSession(event.getSessionId()).close();
        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        chatEndpoint.onConnect(new MultiUserChatSession(session, server));
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        String reason = closeReason.getCloseCode().toString() + ", " + closeReason.getReasonPhrase();
        chatEndpoint.onDisconnect(session.getId(), reason);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        // TODO: Some of these should probably close the ChatSession?
    }
}
