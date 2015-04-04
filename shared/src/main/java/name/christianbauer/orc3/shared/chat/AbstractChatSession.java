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

import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import name.christianbauer.orc3.shared.chat.event.ChannelMessage;
import name.christianbauer.orc3.shared.chat.event.ChatError;
import name.christianbauer.orc3.shared.chat.event.ChatEventHandler;
import name.christianbauer.orc3.shared.chat.event.ChatMessage;

import java.util.logging.Logger;

@JsType
public abstract class AbstractChatSession<T> extends ChatEventHandler {

    private static final Logger LOG = Logger.getLogger(AbstractChatSession.class.getName());

    public interface SendOK {

        public static final SendOK NOOP = new SendOK() {
            @Override
            public void onOK(ChatMessage chatMessage) {
                // NOOP
            }

            @Override
            public String toString() {
                return "NOOP";
            }
        };

        void onOK(ChatMessage chatMessage);
    }

    public interface SendFailure {
        void onFailure(ChatMessage chatMessage, Throwable t);
    }

    final protected T instance;

    public AbstractChatSession(T instance) {
        this.instance = instance;
    }

    public T getSession() {
        return instance;
    }

    public void send(ChatMessage chatMessage) {
        send(chatMessage, getId());
    }

    @JsNoExport
    public void send(ChatMessage chatMessage, SendOK sendOk) {
        doSend(chatMessage, sendOk, (msg, t) -> onError("Sending to peer failed", t), false, getId());
    }

    @JsNoExport
    public void send(ChatMessage chatMessage, String... sessions) {
        doSend(chatMessage, SendOK.NOOP, (msg, t) -> onError("Sending to some peers failed", t), false, sessions);
    }

    public void sendAll(ChatMessage chatMessage) {
        doSend(chatMessage, SendOK.NOOP, (msg, t) -> onError("Sending to some peers failed", t), true);
    }

    public void sendError(String reason) {
        LOG.fine("(" + getId() + ") Sending error to peer: " + reason);
        send(new ChatError(reason));
    }

    public void onReceive(ChatMessage chatMessage) {
        try {
            LOG.fine("(" + getId() + ") Consuming received: " + chatMessage);
            consume(chatMessage);
            // If it's a ChannelMessage, consume a generic event so a single listener can react to all ChannelMessages
            if (chatMessage instanceof ChannelMessage) {
                consume(ChannelMessage.class.getName(), chatMessage);
            }
        } catch (Exception ex) {
            onError("Reception of message failed: " + chatMessage, ex);
        }
    }

    abstract public String getId();

    abstract protected void doSend(ChatMessage chatMessage, SendOK sendOK, SendFailure sendFailure, boolean all, String... sessionIds);

}
