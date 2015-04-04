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

import com.google.gwt.core.client.js.JsType;

@JsType
public abstract class AbstractClientChatEndpoint<SESSION extends AbstractChatSession> extends DefaultChatEndpoint<SESSION> {
    
    public void connect(String serviceUrl) {
        try {
            SESSION session = doConnect(serviceUrl);
            if (session != null)
                onConnect(session);
        } catch (Exception ex) {
            onError("Connect failed", ex);
        }
    }

    public void disconnect(String sessionId) {
        try {
            String reason = "Chat client closing connection.";
            SESSION session = getSession(sessionId);
            if (session != null) {
                doDisconnect(session, reason);
                onDisconnect(sessionId, reason);
            }
        } catch (Exception ex) {
            onError("Disconnect failed", ex);
        }
    }

    abstract protected SESSION doConnect(String serviceUrl) throws Exception;

    abstract protected void doDisconnect(SESSION session, String reason) throws Exception;

}
