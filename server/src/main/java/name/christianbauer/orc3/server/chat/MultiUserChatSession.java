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

import name.christianbauer.orc3.shared.chat.event.*;

import javax.websocket.Session;
import java.util.logging.Logger;

public class MultiUserChatSession extends WebSocketChatSession {

    private static final Logger LOG = Logger.getLogger(MultiUserChatSession.class.getName());

    final protected ChatServer server;

    public MultiUserChatSession(Session instance, ChatServer server) {
        super(instance);
        this.server = server;

        register(ChatNickMessage.class, msg -> {
            LOG.fine("############ (" + getId() + ") Nick message: " + msg.getNick());
            synchronized (server) {
                if (server.isNickTaken(msg.getNick())) {
                    sendError("Nick is already taken: " + msg.getNick());
                    return;
                }
                server.setNick(getId(), msg.getNick());

                send(new ChatNickMessage(msg.getNick()));
                send(new ChannelsListMessage(server.getChannelNames()));
            }
        });

        register(ChannelJoinMessage.class, msg -> {
            LOG.fine("############ (" + getId() + ") Join message: " + msg.getChannelName());
            synchronized (server) {

                if (!server.hasNick(getId())) {
                    sendError("Please register a nick first.");
                    return;
                }

                if (server.isMemberOfChannel(msg.getChannelName(), getId())) {
                    return;
                }

                // First join creates a channel, send channels list to all active sessions
                if (server.addMember(msg.getChannelName(), getId())) {
                    sendAll(new ChannelsListMessage(server.getChannelNames()));
                }

                // Notify all members of channel that someone joined
                send(
                    new ChannelJoinMessage(msg.getChannelName(), server.getNick(getId())),
                    server.getMembersOfChannel(msg.getChannelName())
                );

                // Send member list to all channel sessions
                send(
                    new ChannelMembersMessage(msg.getChannelName(), server.getNicksOfChannelMembers(msg.getChannelName())),
                    server.getMembersOfChannel(msg.getChannelName())
                );
            }
        });

        register(ChannelPartMessage.class, msg -> {
            LOG.fine("############ (" + getId() + ") Part message: " + msg.getChannelName());
            synchronized (server) {

                if (!server.hasNick(getId())) {
                    sendError("Please register a nick first.");
                    return;
                }

                if (!server.isMemberOfChannel(msg.getChannelName(), getId()))
                    return;

                String nick = server.getNick(getId());

                // Notify all members of channel that someone left (including the guy who didn't "yet" leave)
                send(
                    new ChannelPartMessage(msg.getChannelName(), nick),
                    server.getSessionsOfChannelAndPart(msg.getChannelName(), getId())
                );

                // Send member list to all channel sessions
                send(
                    new ChannelMembersMessage(msg.getChannelName(), server.getNicksOfChannelMembers(msg.getChannelName())),
                    server.getMembersOfChannel(msg.getChannelName())
                );

                // Channel might have been closed after parting, send channels list to all active sessions
                sendAll(new ChannelsListMessage(server.getChannelNames()));
            }
        });

        register(ChannelTextMessage.class, msg -> {
            LOG.fine("############ (" + getId() + ") Text message: " + msg.getChannelName());
            synchronized (server) {

                if (!server.hasNick(getId())) {
                    sendError("Please register a nick first.");
                    return;
                }

                if (!server.isMemberOfChannel(msg.getChannelName(), getId()))
                    return;

                String nick = server.getNick(getId());

                // Send message to all members of channel
                send(
                    new ChannelTextMessage(msg.getChannelName(), nick, msg.getText()),
                    server.getMembersOfChannel(msg.getChannelName())
                );
            }
        });
    }

    public void close() {
        LOG.fine("############ (" + getId() + ") Close");
        synchronized (server) {
            String nick = server.getNick(getId());

            String[] channelNames = server.getChannelsOfMember(getId());
            for (String channelName : channelNames) {

                // Notify all members of all channels (of this guy) that she left
                send(
                    new ChannelPartMessage(channelName, nick),
                    server.getSessionsOfChannelAndPart(channelName, getId())
                );

                // Send member list to all channel sessions
                send(
                    new ChannelMembersMessage(channelName, server.getNicksOfChannelMembers(channelName)),
                    server.getMembersOfChannel(channelName)
                );
            }

            // Channel might have been closed after parting, send channels list to all active sessions
            sendAll(new ChannelsListMessage(server.getChannelNames()));

            server.removeNick(getId());
        }
    }
}
