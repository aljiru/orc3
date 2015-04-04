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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.gwt.core.client.js.JsExport;

@JsExport
@JsonTypeName("NICK")
public class ChatNickMessage extends ChatMessage {

    protected String nick;

    protected ChatNickMessage() {
    }

    public ChatNickMessage(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    @Override
    public String toString() {
        return "ChatNickMessage{" +
            "nick='" + nick + '\'' +
            '}';
    }
}
