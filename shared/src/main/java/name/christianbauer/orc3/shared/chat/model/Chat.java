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

import java.util.*;

@JsExport
@JsType
public class Chat {

    public Channel[] channels = new Channel[0];

    public Channel getChannel(String channelName) {
        for (Channel channel : channels) {
            if (channel.name.equals(channelName))
                return channel;
        }
        return null;
    }

    public void clearChannels() {
        channels = new Channel[0];
    }

    public String[] getChannelNames() {
        List<String> list = new ArrayList<>();
        for (Channel channel : channels) {
            list.add(channel.name);
        }
        return list.toArray(new String[list.size()]);
    }

    public void updateChannels(String[] channelNames) {
        List<String> desiredChannelNames = Arrays.asList(channelNames);

        List<String> newChannelNames = new ArrayList<>();
        for (String channelName : channelNames) {
            if (getChannel(channelName) == null)
                newChannelNames.add(channelName);
        }

        List<String> obsoleteChannelNames = new ArrayList<>();
        for (String channelName : getChannelNames()) {
            if (!desiredChannelNames.contains(channelName))
                obsoleteChannelNames.add(channelName);
        }

        List<Channel> list = new ArrayList<>(Arrays.asList(channels));

        Iterator<Channel> it = list.iterator();
        while (it.hasNext()) {
            Channel channel = it.next();
            if (obsoleteChannelNames.contains(channel.name))
                it.remove();
        }

        for (String newChannelName : newChannelNames) {
            list.add(new Channel(newChannelName));
        }

        channels = list.toArray(new Channel[list.size()]);
    }

    public boolean addMember(String channelName, String member) {
        boolean newChannel = false;
        Channel channel = getChannel(channelName);
        if (channel == null) {
            channel = new Channel(channelName);
            newChannel = true;
        }

        channel.addMember(member);

        if (newChannel) {
            List<Channel> list = new ArrayList<>(Arrays.asList(channels));
            list.add(channel);
            channels = list.toArray(new Channel[list.size()]);
        }

        return newChannel;
    }

    public void removeMember(String channelName, String member) {
        for (Channel channel : channels) {
            if (channel.name.equals(channelName)) {
                channel.removeMember(member);
            }
        }
    }

    public void setMembers(String channelName, String[] members) {
        Channel channel = getChannel(channelName);
        if (channel == null)
            return;
        channel.setMembers(members);
    }

    public String[] getMembersOfChannel(String channelName) {
        Channel channel = getChannel(channelName);
        if (channel == null)
            return new String[0];
        return channel.members;
    }

    public boolean isMemberOfChannel(String channelName, String member) {
        Channel channel = getChannel(channelName);
        return channel != null && channel.isMember(member);
    }

    public String[] getChannelsOfMember(String member) {
        Set<String> result = new HashSet<>();
        for (String channelName : getChannelNames()) {
            if (isMemberOfChannel(channelName, member))
                result.add(channelName);
        }
        return result.toArray(new String[result.size()]);
    }

    public int getNumberOfChannelMembers(String channelName) {
        Channel channel = getChannel(channelName);
        if (channel == null)
            return 0;
        return channel.size();
    }

    protected void closeEmptyChannels() {
        List<Channel> list = new ArrayList<>(Arrays.asList(channels));
        Iterator<Channel> it = list.iterator();
        while (it.hasNext()) {
            Channel channel = it.next();
            if (channel.isEmpty())
                it.remove();
        }
        channels = list.toArray(new Channel[list.size()]);
    }

    @Override
    public String toString() {
        return "Chat{" +
            "channels=" + Arrays.toString(channels) +
            '}';
    }
}
