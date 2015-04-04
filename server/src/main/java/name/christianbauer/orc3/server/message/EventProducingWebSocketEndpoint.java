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

import org.jboss.weld.context.bound.BoundRequestContext;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transforms all WebSocket messages to CDI events, and wraps them in request context.
 * TODO: Wrap in session context
 */
@Singleton
public class EventProducingWebSocketEndpoint extends Endpoint {

    private static final Logger LOG = Logger.getLogger(EventProducingWebSocketEndpoint.class.getName());

    @Inject
    BoundRequestContext requestContext;

    @Inject
    private Event<Message> messageEvents;

    @Inject
    private Event<OpenSession> openSessionEvents;

    @Inject
    private Event<CloseSession> closeSessionEvents;

    @Inject
    private Event<SessionError> sessionErrorEvents;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        if (LOG.isLoggable(Level.FINE))
            LOG.fine("WebSocket open session: " + session.getId());

        Map<String, Object> openSessionRequestContext = startRequest();
        openSessionEvents.fire(new OpenSession(session, config));
        endRequest(openSessionRequestContext);

        session.addMessageHandler(String.class, text -> {
            if (LOG.isLoggable(Level.FINE))
                LOG.fine("WebSocket message in session: " + session.getId());
            Map<String, Object> messageRequestContext = startRequest();
            messageEvents.fire(new Message(session, text));
            endRequest(messageRequestContext);
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (LOG.isLoggable(Level.FINE))
            LOG.fine("WebSocket close session: " + session.getId() + ", reason: " + closeReason);
        Map<String, Object> requestContext = startRequest();
        closeSessionEvents.fire(new CloseSession(session, closeReason));
        endRequest(requestContext);
    }


    @Override
    public void onError(Session session, Throwable throwable) {
        if (LOG.isLoggable(Level.FINE))
            LOG.fine("WebSocket error session: " + session.getId() + " => " + throwable);
        Map<String, Object> requestContext = startRequest();
        sessionErrorEvents.fire(new SessionError(session, throwable));
        endRequest(requestContext);
    }

    public Map<String, Object> startRequest() {
        Map<String, Object> store = new HashMap<>();
        requestContext.associate(store);
        requestContext.activate();
        return store;
    }

    public void endRequest(Map<String, Object> store) {
        try {
            requestContext.invalidate();
            requestContext.deactivate();
        } finally {
            requestContext.dissociate(store);
        }
        store.clear();
    }

}
