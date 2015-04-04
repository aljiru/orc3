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
package name.christianbauer.orc3.server.chat;

import name.christianbauer.orc3.shared.chat.model.Chat;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ChatServer extends Chat {

    protected Map<String, String> sessions = new HashMap<>();

    public String getNick(String sessionId) {
        return sessions.get(sessionId);
    }

    public void setNick(String sessionId, String nick) {
        sessions.put(sessionId, nick);
    }

    public boolean hasNick(String sessionId) {
        return getNick(sessionId) != null;
    }

    public boolean isNickTaken(String nick) {
        return sessions.containsValue(nick);
    }

    public void removeNick(String sessionId) {
        sessions.remove(sessionId);
    }

    public String[] getNicksOfChannelMembers(String channelName) {
        List<String> nicks = new ArrayList<>();
        for (String sessionId : getMembersOfChannel(channelName)) {
            nicks.add(getNick(sessionId));
        }
        return nicks.toArray(new String[nicks.size()]);
    }

    public String[] getSessionsOfChannelAndPart(String channelName, String sessionId) {
        String[] sessions = getMembersOfChannel(channelName);
        removeMember(channelName, sessionId);
        closeEmptyChannels();
        return sessions;
    }

    @Override
    public String toString() {
        return "ChatServer{" +
            "sessions=" + sessions +
            "} " + super.toString();
    }
}
