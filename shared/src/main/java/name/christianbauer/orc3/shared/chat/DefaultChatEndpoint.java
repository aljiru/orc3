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
import name.christianbauer.orc3.shared.chat.event.ChatConnected;
import name.christianbauer.orc3.shared.chat.event.ChatDisconnected;
import name.christianbauer.orc3.shared.chat.event.ChatEventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@JsType
public class DefaultChatEndpoint<SESSION extends AbstractChatSession> extends ChatEventHandler {

    private static final Logger LOG = Logger.getLogger(DefaultChatEndpoint.class.getName());

    final protected Map<String, SESSION> sessions = new HashMap<>();

    public void onConnect(SESSION session) {
        LOG.fine("On connect: " + session.getId());
        synchronized (sessions) {
            sessions.put(session.getId(), session);
            consume(new ChatConnected(session.getId()));
        }
    }

    public void onDisconnect(String sessionId, String reason) {
        LOG.fine("On disconnect: " + sessionId + " => " + reason);
        synchronized (sessions) {
            if (sessions.containsKey(sessionId)) {
                consume(new ChatDisconnected(sessionId, reason));
                sessions.remove(sessionId);
            }
        }
    }

    public SESSION getSession(String sessionId) {
        synchronized (sessions) {
            return sessions.get(sessionId);
        }
    }

}
