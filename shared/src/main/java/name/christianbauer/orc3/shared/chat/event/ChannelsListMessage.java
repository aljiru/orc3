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
import com.google.gwt.core.client.js.JsType;

import java.util.Arrays;

@JsExport
@JsType
@JsonTypeName("LIST")
public class ChannelsListMessage extends ChatMessage {

    protected String[] channelNames;

    protected ChannelsListMessage() {
    }

    public ChannelsListMessage(String[] channelNames) {
        this.channelNames = channelNames;
    }

    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String toString() {
        return "ChannelsListMessage{" +
            "channelNames=" + Arrays.toString(channelNames) +
            '}';
    }
}
