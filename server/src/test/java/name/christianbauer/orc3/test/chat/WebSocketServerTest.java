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
package name.christianbauer.orc3.test.chat;

import name.christianbauer.orc3.server.Config;
import name.christianbauer.orc3.server.Main;
import org.glassfish.tyrus.client.ClientManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import javax.websocket.WebSocketContainer;
import java.net.Inet4Address;
import java.net.ServerSocket;

public abstract class WebSocketServerTest {

    protected String host;
    protected int port;
    protected String serviceUrl;

    @BeforeClass
    public void startServer() throws Exception {
        // TODO: Undertow doesn't want to let me know its ephemeral port, so we pick one, another victory for private fields!
        try {
            ServerSocket socket = new ServerSocket(0, 0, Inet4Address.getLocalHost());

            host = "localhost";
            port = socket.getLocalPort();
            serviceUrl = "ws://" + host + ":" + port + "/v1" + getServicePath();

            System.setProperty(Config.WEBSERVER_ADDRESS, host);
            System.setProperty(Config.WEBSERVER_PORT, Integer.toString(port));
            socket.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        Main.main(new String[0]);
    }

    @AfterClass
    public void stopServer() throws Exception {
        Main.shutdownNow();
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    protected abstract String getServicePath();

    protected WebSocketContainer getWebSocketClientContainer() {
        return ClientManager.createClient();
    }
}
