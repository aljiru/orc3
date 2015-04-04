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
package name.christianbauer.orc3.shared.chat;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import name.christianbauer.orc3.shared.chat.event.*;
import name.christianbauer.orc3.shared.chat.model.Channel;
import name.christianbauer.orc3.shared.chat.model.ChannelItem;
import name.christianbauer.orc3.shared.chat.model.Chat;

import java.util.logging.Logger;

@JsExport
@JsType
public class ChatClient extends Chat {

    private static final Logger LOG = Logger.getLogger(ChatClient.class.getName());

    public AbstractClientChatEndpoint endpoint;

    public String server;

    public String nick;

    public String status;

    public AbstractChatSession session;

    public boolean sessionActive;

    public Channel currentChannel;

    public ChatClient(AbstractClientChatEndpoint endpoint) {
        this.endpoint = endpoint;

        endpoint.register(ChatConnected.class, e -> {
            session = endpoint.getSession(e.getSessionId());

            session.register(ChatError.class, sessionError -> {
                setStatus(sessionError.getReason());
            });

            session.register(ChatNickMessage.class, msg -> {
                if (msg.getNick().equals(nick)) {
                    sessionActive = true;
                    setStatus("Connected to server with nick: " + msg.getNick());
                }
            });

            session.register(ChannelsListMessage.class, msg -> {
                updateChannels(msg.getChannelNames());
            });

            session.register(ChannelMembersMessage.class, msg -> {
                setMembers(msg.getChannelName(), msg.getMembers());
            });

            session.register(ChannelJoinMessage.class, msg -> {
                Channel channel = getChannel(msg.getChannelName());
                if (channel == null)
                    return;
                if (msg.getMember().equals(nick) && !channel.active) {
                    channel.activate();
                    currentChannel = channel;
                    setStatus("Joining channel: " + channel.name);

                } else if (channel.active) {
                    channel.addItem(new ChannelItem(
                        true,
                        msg.getMember() + " joined channel."
                    ).setNick(msg.getMember()));
                }
            });

            session.register(ChannelPartMessage.class, msg -> {
                Channel channel = getChannel(msg.getChannelName());
                if (channel == null)
                    return;
                if (channel.active) {
                    channel.addItem(new ChannelItem(
                        true,
                        msg.getMember() + " left channel."
                    ).setNick(msg.getMember()));
                }
            });

            session.register(ChannelTextMessage.class, msg -> {
                Channel channel = getChannel(msg.getChannelName());
                if (channel == null)
                    return;
                if (channel.active) {
                    channel.addItem(
                        new ChannelItem(false, msg.getText())
                            .setNick(msg.getMember())
                            .setOwn(msg.getMember().equals(nick))
                    );
                }
            });

            sendNick();
        });

        endpoint.register(ChatDisconnected.class, e -> {
            setStatus(e.getReason());
            session = null;
            sessionActive = false;
            currentChannel = null;
            clearChannels();
        });

        endpoint.register(ChatError.class, e -> {
            setStatus(e.getReason());
        });
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    public void connect() {
        // Open a session if it's not open or try again to register a nick
        if (session == null) {
            endpoint.connect(server);
        } else {
            sendNick();
        }
    }

    public void disconnect() {
        if (session != null) {
            endpoint.disconnect(session.getId());
        }
    }

    public void sendNick() {
        if (session != null) {
            session.send(new ChatNickMessage(nick));
        }
    }

    public void join(String channelName) {
        if (channelName == null)
            return;

        // TODO Bean validation?
        if (channelName.length() > 15) {
            setStatus("Channel names must be maximum 15 characters long.");
            return;
        }

        Channel channel = getChannel(channelName);
        if (session != null && (channel == null || !channel.active)) {
            session.send(new ChannelJoinMessage(channelName));
        }
    }

    public void part(String channelName) {
        Channel channel = getChannel(channelName);
        if (session != null && channel != null && channel.active) {
            channel.deactivate();
            currentChannel = null;
            setStatus("Left channel: " + channelName);
            session.send(new ChannelPartMessage(channelName));
        }
    }

    public void sendText(String channelName, String text) {
        if (text == null || text.length() == 0)
            return;
        session.send(new ChannelTextMessage(channelName, text));
    }

}
