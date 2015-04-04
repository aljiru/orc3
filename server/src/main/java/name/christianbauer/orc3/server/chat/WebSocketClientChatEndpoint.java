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

import name.christianbauer.orc3.shared.chat.AbstractClientChatEndpoint;

import javax.websocket.*;
import java.net.URI;

import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

public abstract class WebSocketClientChatEndpoint extends AbstractClientChatEndpoint<WebSocketChatSession> {

    final protected ClientEndpointConfig clientEndpointConfig;
    final protected Endpoint endpoint;

    public WebSocketClientChatEndpoint() {
        this(ClientEndpointConfig.Builder.create().build());
    }

    public WebSocketClientChatEndpoint(ClientEndpointConfig clientEndpointConfig) {
        this.clientEndpointConfig = clientEndpointConfig;
        this.endpoint = new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                // Since connectToServer() blocks, we don't have to do anything in this callback
            }

            @Override
            public void onClose(Session session, CloseReason closeReason) {
                if (!closeReason.getCloseCode().equals(NORMAL_CLOSURE)) {
                    onDisconnect(session.getId(), closeReason.getReasonPhrase());
                }
            }

            @Override
            public void onError(Session session, Throwable thr) {
                // TODO: Some of these should probably close the ChatSession?
            }
        };
    }

    @Override
    protected WebSocketChatSession doConnect(String serviceUrl) throws Exception {
        return createSession(
            getWebSocketContainer().connectToServer(
                endpoint,
                clientEndpointConfig,
                URI.create(serviceUrl)
            )
        );
    }

    @Override
    protected void doDisconnect(WebSocketChatSession session, String reason) throws Exception {
        session.getSession().close(
            new CloseReason(NORMAL_CLOSURE, reason)
        );
    }

    protected abstract WebSocketContainer getWebSocketContainer();
    protected abstract WebSocketChatSession createSession(Session session);

}
