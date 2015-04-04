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
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import name.christianbauer.orc3.shared.chat.event.ChatMessage;
import name.christianbauer.orc3.shared.chat.AbstractClientChatEndpoint;
import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.WebSocketListener;

import java.util.logging.Logger;

@JsExport
public class ChatEndpoint extends AbstractClientChatEndpoint<ChatSession> implements WebSocketListener {

    private static final Logger LOG = Logger.getLogger(ChatEndpoint.class.getName());

    final ChatMessageCodec JSON_CODEC = GWT.create(ChatMessageCodec.class);

    protected String serviceUrl;

    @Override
    protected ChatSession doConnect(String serviceUrl) throws Exception {
        WebSocket webSocket = WebSocket.newWebSocketIfSupported();

        if (webSocket == null) {
            onError("WebSockets not supported in this browser... oops");
            return null;
        }

        webSocket.setListener(this);

        try {
            webSocket.connect(serviceUrl);
        } catch (Exception ex) {
            onError("Error connecting WebSocket: " + ex);
        }

        // Asynchronous connection, see onOpen()
        return null;
    }

    @Override
    protected void doDisconnect(ChatSession session, String reason) throws Exception {
        short normalClose = 1000;
        session.getSession().close(normalClose, "Client explicitly disconnected.");
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.serviceUrl = webSocket.getURL();
        onConnect(new ChatSession(webSocket));
    }

    @Override
    public void onClose(WebSocket webSocket, boolean wasClean, int code, String reason) {
        if (code != 1000) {
            onDisconnect(webSocket.getURL(), reason);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String data) {
        try {
            ChatMessage chatMessage = JSON_CODEC.decode(data);
            getSession(serviceUrl).onReceive(chatMessage);
        } catch (Exception ex) {
            onError("Parsing received message failed: " + data , ex);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ArrayBuffer data) {
        onError("Can't receive binary data (yet)");
    }

    @Override
    public void onError(WebSocket webSocket) {
        onError("Connection failed, check server address");
    }
}
