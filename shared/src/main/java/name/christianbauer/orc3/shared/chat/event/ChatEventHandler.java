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
package name.christianbauer.orc3.shared.chat.event;

import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsType
public class ChatEventHandler {

    private static final Logger LOG = Logger.getLogger(ChatEventHandler.class.getName());

    public static final String ERROR_EVENT_NAME = ChatError.class.getName();

    final protected Collection<ChatRegistration> registrations = new ArrayList<>();

    @JsNoExport
    public <E extends ChatEvent> ChatRegistration<E> register(Class<E> eventClass, ChatListener<E> listener) {
        return register(eventClass.getName(), listener);
    }

    public <E extends ChatEvent> ChatRegistration<E> register(String eventName, ChatListener<E> listener) {
        LOG.fine("Registering listener for: " + eventName);
        ChatRegistration<E> registration = new ChatRegistration<>(eventName, listener);
        registrations.add(registration);
        return registration;
    }

    public void unregister(ChatRegistration registration) {
        registrations.remove(registration);
    }

    @JsNoExport
    public void consume(ChatEvent event) {
        consume(event.getClass().getName(), event);
    }

    @SuppressWarnings("unchecked")
    public void consume(String eventName, ChatEvent someEvent) {
        LOG.fine("Consuming event: " + eventName);
        for (ChatRegistration registration : registrations) {
            if (registration.getEventName().equals(eventName)) {
                try {
                    if (ERROR_EVENT_NAME.equals(eventName)) {
                        LOG.fine("Consuming error event: " + someEvent.toString());
                    }
                    registration.getListener().on(someEvent);
                } catch (Throwable t) {
                    if (!ERROR_EVENT_NAME.equals(eventName)) {
                        onError("Consuming event '" + eventName + "' failed", t);
                    }
                }
            }
        }
    }

    protected void onError(String reason) {
        onError(reason, null);
    }

    @JsNoExport
    protected void onError(String reason, Throwable t) {
        LOG.log(Level.FINE, "On error: " + reason, t);
        consume(ERROR_EVENT_NAME, new ChatError(
            t != null ? reason + ": " + t.getMessage() : reason + "."
        ));
    }

}
