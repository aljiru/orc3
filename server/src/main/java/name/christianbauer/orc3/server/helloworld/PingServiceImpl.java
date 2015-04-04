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
package name.christianbauer.orc3.server.helloworld;

import name.christianbauer.orc3.shared.helloworld.Ping;
import name.christianbauer.orc3.shared.helloworld.PingService;
import name.christianbauer.orc3.shared.helloworld.Pong;

import java.util.logging.Logger;

public class PingServiceImpl implements PingService {

    private static final Logger LOG = Logger.getLogger(PingServiceImpl.class.getName());

    @Override
    public Pong doPing(Ping ping) {
        LOG.info("Ping from client, returning pong: " + ping);
        return ping != null ? new Pong(ping.getPayload()) : new Pong();
    }

    @Override
    public String hello() {
        return "HELLO WORLD";
    }
}
