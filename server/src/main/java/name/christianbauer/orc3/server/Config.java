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

package name.christianbauer.orc3.server;

public interface Config {

    // If enabled, show stack traces in error responses and exception logging
    String DEVMODE = "DEVMODE";
    String DEVMODE_DEFAULT = "true";

    // For API URL versioning under the same server/authority (yes, yes, it's evil)
    String API_VERSION = "API_VERSION";
    String API_VERSION_DEFAULT = "1";

    String WEBSERVER_PORT = "WEBSERVER_PORT";
    String WEBSERVER_PORT_DEFAULT = "8080";

    String WEBSERVER_ADDRESS = "WEBSERVER_ADDRESS";
    String WEBSERVER_ADDRESS_DEFAULT = "127.0.0.1";

    String WEBSERVER_WEBAPP_DIRECTORY = "WEBSERVER_WEBAPP_DIRECTORY";
    String WEBSERVER_WEBAPP_DIRECTORY_DEFAULT = "client/src/main/webapp";

}
