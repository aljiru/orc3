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
package name.christianbauer.orc3.server.message;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

public class OpenSession extends AbstractExchange {

    protected EndpointConfig config;

    public OpenSession(Session peer, EndpointConfig config) {
        super(peer);
        this.config = config;
    }
}
