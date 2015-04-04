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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import name.christianbauer.orc3.shared.chat.AbstractChatSession;
import name.christianbauer.orc3.shared.chat.event.ChatMessage;

import javax.websocket.MessageHandler;
import javax.websocket.SendHandler;
import javax.websocket.Session;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class WebSocketChatSession extends AbstractChatSession<Session> implements MessageHandler.Whole<String> {

    private static final Logger LOG = Logger.getLogger(WebSocketChatSession.class.getName());

    final static protected ObjectReader JSON_READER = new ObjectMapper().reader(ChatMessage.class);
    final static protected ObjectWriter JSON_WRITER = new ObjectMapper().writer();

    public WebSocketChatSession(Session instance) {
        super(instance);
        instance.addMessageHandler(this);
    }

    @Override
    public String getId() {
        return getSession().getId();
    }

    @Override
    protected void doSend(ChatMessage chatMessage, SendOK sendOK, SendFailure sendFailure, boolean all, String... sessionIds) {
        if (all) {
            LOG.fine("(" + getId() + ") Sending to all peers: " + chatMessage);
        } else if (sessionIds.length == 0) {
            LOG.fine("(" + getId() + ") Skipping message, no peers given to send: " + chatMessage);
        } else if (sessionIds.length == 1 && Arrays.asList(sessionIds).contains(getId())) {
            LOG.fine("(" + getId() + ") Sending to my peer: " + chatMessage);
        } else {
            LOG.fine("(" + getId() + ") Sending to peers '" + Arrays.toString(sessionIds) + "': " + chatMessage);
        }

        if (!all && sessionIds.length == 0)
            return; // No peers given and not sending to all

        String text;
        try {
            text = JSON_WRITER.writeValueAsString(chatMessage);
        } catch (Exception ex) {
            sendFailure.onFailure(chatMessage, ex);
            return;
        }

        SendHandler sendHandler = result -> {
            if (result.isOK())
                sendOK.onOK(chatMessage);
            else
                sendFailure.onFailure(chatMessage, result.getException());
        };

        // Send to all open sessions as requested; if no session identifiers are given, send to all
        List<String> sessionIdList = sessionIds != null ? Arrays.asList(sessionIds) : Collections.EMPTY_LIST;
        for (Session session : getSession().getOpenSessions()) {
            boolean isApplicable = all || (sessionIdList.contains(session.getId()));
            if (session.isOpen() && isApplicable) {
                session.getAsyncRemote().sendText(text, sendHandler);
            } else {
                LOG.fine("(" + getId() + ") Skipping message, peer is not open or applicable '" + session.getId() + "': " + chatMessage);
            }
        }
    }

    @Override
    public void onMessage(String text) {
        try {
            onReceive(JSON_READER.readValue(text));
        } catch (Exception ex) {
            onError("Parsing received message failed: " + text, ex);
        }
    }
}
