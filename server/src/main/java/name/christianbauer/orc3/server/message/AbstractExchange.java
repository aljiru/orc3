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
package name.christianbauer.orc3.server.message;

import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

public abstract class AbstractExchange {

    static public SendHandler FIRE_AND_FORGET = new SendHandler() {
        @Override
        public void onResult(SendResult result) {
            // Ignore
        }
    };

    final Session session;

    public AbstractExchange(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void send(String text, SendHandler sendHandler) {
        send(getSession(), text, sendHandler);
    }

    public void send(Session s, String text, SendHandler sendHandler) {
        send(s, text, 3000, sendHandler);
    }

    public void send(Session s, String text, int timeoutMillis, SendHandler sendHandler) {
        RemoteEndpoint.Async remote = s.getAsyncRemote();
        remote.setSendTimeout(timeoutMillis);
        remote.sendText(text, sendHandler);
    }

    public void sendAll(String text, SendHandler sendHandler) {
        getSession().getOpenSessions().forEach(session ->
                send(session, text, sendHandler)
        );
    }
}
