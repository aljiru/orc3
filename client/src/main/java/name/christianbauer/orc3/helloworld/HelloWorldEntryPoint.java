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

package name.christianbauer.orc3.helloworld;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import name.christianbauer.orc3.shared.helloworld.Ping;
import name.christianbauer.orc3.shared.helloworld.PingService;
import name.christianbauer.orc3.shared.helloworld.Pong;
import org.fusesource.restygwt.client.*;

import java.util.logging.Logger;

public class HelloWorldEntryPoint implements com.google.gwt.core.client.EntryPoint {

    private static Logger LOG = Logger.getLogger(HelloWorldEntryPoint.class.getName());

    FlowPanel panel = new FlowPanel();

    @Override
    public void onModuleLoad() {

        RootLayoutPanel.get().add(panel);

        configureModule();

        panel.add(new Label("Calling GET hello on JAX-RS service..."));
        Resource resource = new Resource(GWT.getHostPageBaseURL() + "v1/ping");
        resource.get().send(
            new TextCallback() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    LOG.severe("Oops, it didn't work.");
                }

                @Override
                public void onSuccess(Method method, String response) {
                    panel.add(new Label("JAX-RS says (" + method.getResponse().getStatusCode() + "): " + response));

                }
            }
        );

        panel.add(new Label("Sending Ping to JAX-RS service..."));
        PingService pingService = GWT.create(PingService.class);
        REST.withCallback(new DefaultServiceCallback<Pong>() {
            @Override
            public void onSuccess(Method method, Pong response) {
                panel.add(new Label("JAX-RS says (" + method.getResponse().getStatusCode() + "): " + response));

            }
        }).call(pingService).doPing(new Ping("Hello JAX-RS"));
    }

    protected void configureModule() {
        // REST path prefix
        Defaults.setServiceRoot("/v1");
    }

    abstract class DefaultServiceCallback<T> implements MethodCallback<T> {
        @Override
        public void onFailure(Method method, Throwable exception) {
            if (method.getResponse() != null) {
                LOG.severe("Failure calling service, response status: "
                        + method.getResponse().getStatusCode() + " " + method.getResponse().getStatusText()
                );
            } else {
                LOG.severe("Failure calling service, no response.");
            }
        }
    }

}
