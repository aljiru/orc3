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
package name.christianbauer.orc3.shared.chat.model;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;

import java.util.Date;

@JsExport
@JsType
public class ChannelItem {

    public Date timestamp;

    public boolean control;

    public boolean own;

    public String nick;

    public String text;

    public ChannelItem(boolean control, String text) {
        this.timestamp = new Date();
        this.control = control;
        this.text = text;
    }

    public ChannelItem setOwn(boolean own) {
        this.own = own;
        return this;
    }

    public ChannelItem setNick(String nick) {
        this.nick = nick;
        return this;
    }

    @Override
    public String toString() {
        return "ChannelItem{" +
            "timestamp=" + timestamp +
            ", control=" + control +
            ", own=" + own +
            ", nick='" + nick + '\'' +
            ", text='" + text + '\'' +
            '}';
    }
}
