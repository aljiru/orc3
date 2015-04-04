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

import java.util.Arrays;

@JsExport
@JsonTypeName("MEMBERS")
public class ChannelMembersMessage extends ChannelMessage {

    protected String[] members;

    protected ChannelMembersMessage() {
    }

    public ChannelMembersMessage(String channel, String[] members) {
        super(channel);
        this.members = members;
    }

    public String[] getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "ChannelMembersMessage{" +
            "members=" + Arrays.toString(members) +
            "} " + super.toString();
    }
}
