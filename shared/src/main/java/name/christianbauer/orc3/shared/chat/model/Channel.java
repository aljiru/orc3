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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@JsExport
@JsType
public class Channel {

    public String name;

    public String[] members = new String[0];

    public boolean active;

    public ChannelItem[] items = new ChannelItem[0];

    public Channel(String name) {
        this.name = name;
    }

    public void addMember(String member) {
        List<String> list = new ArrayList<>(Arrays.asList(members));
        list.add(member);
        members = list.toArray(new String[list.size()]);
    }

    public void removeMember(String member) {
        List<String> list = new ArrayList<>(Arrays.asList(members));
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().equals(member))
                it.remove();
        }
        members = list.toArray(new String[list.size()]);
    }

    public void setMembers(String[] members) {
        this.members = members;
    }

    public boolean isMember(String member) {
        for (String m : members) {
            if (m.equals(member))
                return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return members.length;
    }

    public void addItem(ChannelItem item) {
        List<ChannelItem> list = new ArrayList<>(Arrays.asList(items));
        list.add(item);
        items = list.toArray(new ChannelItem[list.size()]);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
        this.members = new String[0];
        this.items = new ChannelItem[0];
    }

    @Override
    public String toString() {
        return "Channel{" +
            "name='" + name + '\'' +
            ", active=" + active +
            ", members=" + Arrays.toString(members) +
            ", items=" + items.length +
            '}';
    }
}
