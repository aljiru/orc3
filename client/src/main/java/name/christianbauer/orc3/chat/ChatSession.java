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
package name.christianbauer.orc3.chat;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import name.christianbauer.orc3.shared.chat.AbstractChatSession;
import name.christianbauer.orc3.shared.chat.event.ChatMessage;
import org.realityforge.gwt.websockets.client.WebSocket;

import java.util.Arrays;
import java.util.logging.Logger;

@JsExport
public class ChatSession extends AbstractChatSession<WebSocket> {

    private static final Logger LOG = Logger.getLogger(ChatSession.class.getName());

    final ChatMessageCodec JSON_CODEC = GWT.create(ChatMessageCodec.class);

    public ChatSession(WebSocket instance) {
        super(instance);
    }

    @Override
    public String getId() {
        return getSession().getURL();
    }

    @Override
    protected void doSend(ChatMessage chatMessage, SendOK sendOK, SendFailure sendFailure, boolean all, String... sessionIds) {
        // Can obviously only send to peer and not all sessions
        if (!all && Arrays.asList(sessionIds).contains(getId())) {
            String text;
            try {
                text = JSON_CODEC.encode(chatMessage).toString();
                getSession().send(text);
                sendOK.onOK(chatMessage);
            } catch (Exception ex) {
                sendFailure.onFailure(chatMessage, ex);
            }
        }
    }

}
