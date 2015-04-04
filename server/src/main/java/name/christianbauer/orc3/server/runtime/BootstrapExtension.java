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
package name.christianbauer.orc3.server.runtime;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.util.MimeMappings;
import io.undertow.websockets.jsr.DefaultContainerConfigurator;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import name.christianbauer.orc3.server.message.EventProducingWebSocketEndpoint;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.util.GetRestful;
import org.jboss.weld.servlet.WeldInitialListener;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.System.getProperty;
import static name.christianbauer.orc3.server.Config.*;

public class BootstrapExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(BootstrapExtension.class.getName());

    protected Undertow UNDERTOW;
    protected ResteasyDeployment resteasyDeployment = new ResteasyDeployment();
    protected Map<String, Class<? extends EventProducingWebSocketEndpoint>> webSocketEndpoints = new HashMap<>();

    protected <T> void registerResources(@Observes ProcessAnnotatedType<T> event) {
        Class beanClass = event.getAnnotatedType().getJavaClass();
        if (GetRestful.isRootResource(beanClass)) {
            LOG.info("Discovered JAX-RS resource bean class: " + beanClass.getName());
            resteasyDeployment.getActualResourceClasses().add(beanClass);
        }
    }

    protected <T> void registerProviders(@Observes @WithAnnotations(Provider.class) ProcessAnnotatedType<T> event) {
        Class beanClass = event.getAnnotatedType().getJavaClass();
        if (!resteasyDeployment.getActualProviderClasses().contains(beanClass)) {
            LOG.info("Discovered JAX-RS provider bean class: " + beanClass.getName());
            resteasyDeployment.getActualProviderClasses().add(beanClass);
        }
    }

    protected <T> void registerWebSocketEndpoints(@Observes @WithAnnotations(ServerEndpoint.class) ProcessAnnotatedType<T> event) {
        Class beanClass = event.getAnnotatedType().getJavaClass();
        LOG.info("Discovered websocket endpoint class: " + beanClass.getName());
        ServerEndpoint serverEndpoint = event.getAnnotatedType().getAnnotation(ServerEndpoint.class);
        webSocketEndpoints.put(serverEndpoint.value(), beanClass);

    }

    public void start(@Observes AfterDeploymentValidation event) {

        try {
            int port = Integer.valueOf(getProperty(WEBSERVER_PORT, WEBSERVER_PORT_DEFAULT));
            String address = getProperty(WEBSERVER_ADDRESS, WEBSERVER_ADDRESS_DEFAULT);
            String versionPath = "/v" + System.getProperty(API_VERSION, API_VERSION_DEFAULT);

            LOG.info("Starting HTTP/websocket server on: " + address + ":" + port);

            // Connect the electrodes to the brain, wait for lightning
            Undertow server = Undertow.builder()
                .addHttpListener(port, address)
                .setHandler(getHandler(versionPath))
                .build();


            server.start();
            LOG.info("HTTP/websocket server ready on: " + address + ":" + port);

            this.UNDERTOW = server;

        } catch (Exception ex) {
            event.addDeploymentProblem(ex);
        }
    }

    protected HttpHandler getHandler(String versionPath) throws Exception {
        return Exceptions.wrap(
            Handlers.path()
                .addPrefixPath("/", getStaticResourceHandler())
                .addPrefixPath(versionPath, getDynamicResourceHandler(versionPath))
        );
    }

    protected HttpHandler getStaticResourceHandler() throws Exception {
        File webappDirectory = new File(System.getProperty(WEBSERVER_WEBAPP_DIRECTORY, WEBSERVER_WEBAPP_DIRECTORY_DEFAULT));
        if (!webappDirectory.isDirectory())
            throw new FileNotFoundException("Missing " + WEBSERVER_WEBAPP_DIRECTORY + ": " + webappDirectory.getAbsolutePath());
        LOG.info("Configuring static resource handler for filesystem path " + webappDirectory.getAbsolutePath());
        ResourceManager staticResourcesManager = new FileResourceManager(webappDirectory, 0, true, false);

        MimeMappings.Builder mimeBuilder = MimeMappings.builder(true);
        mimeBuilder.addMapping("wsdl", "application/xml");
        mimeBuilder.addMapping("xsl", "text/xsl");
        // TODO: Add more mime/magic stuff?

        return Handlers.resource(staticResourcesManager)
            .setMimeMappings(mimeBuilder.build());
    }

    protected HttpHandler getDynamicResourceHandler(String versionPath) throws Exception {

        // JAX-RS
        LOG.info("Configuring JAX-RS service handler with request path mapping: " +  versionPath + "/*");
        resteasyDeployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        ServletInfo resteasyServlet = Servlets.servlet("RESTEasy Servlet", HttpServlet30Dispatcher.class)
            .setAsyncSupported(true)
            .setLoadOnStartup(1)
            .addMapping("/*");

        // WebSockets
        LOG.info("Configuring event producing WebSocket endpoint with request path mapping: " +  versionPath + "/websocket");
        ServerEndpointConfig websocketConfig = ServerEndpointConfig.Builder.create(EventProducingWebSocketEndpoint.class, "/websocket")
            .configurator(new DefaultContainerConfigurator() {
                @Override
                public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                    return CDI.current().select(endpointClass).get();
                }
            })
            .build();
        WebSocketDeploymentInfo websocketDeploymentInfo =
            new WebSocketDeploymentInfo().addEndpoint(websocketConfig);

        for (Map.Entry<String, Class<? extends EventProducingWebSocketEndpoint>> entry : webSocketEndpoints.entrySet()) {
            String path = entry.getKey().startsWith("/") ? entry.getKey() : "/" + entry.getKey();
            LOG.info("Configuring WebSocket endpoint '" + entry.getValue().getName() + "' with request path mapping: " +  versionPath + path);
            websocketConfig = ServerEndpointConfig.Builder.create(entry.getValue(), path)
                .configurator(new DefaultContainerConfigurator() {
                    @Override
                    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                        return CDI.current().select(endpointClass).get();
                    }
                })
                .build();
            websocketDeploymentInfo.addEndpoint(websocketConfig);
        }

        // TODO 8 threads default OK, make configurable?
        // [main            ] WARN   - 22:38:04,551 - ertow.websockets.jsr.Bootstrap#handleDeployment: UT026009: XNIO worker was not set on WebSocketDeploymentInfo, the default worker will be used
        // TODO: Actually, XNIO worker and buffer pool should be configurable...

        // Wrap it all in one deployment
        DeploymentInfo deploymentInfo = new DeploymentInfo()
            .addListener(Servlets.listener(WeldInitialListener.class))
            .setContextPath(versionPath)
            .addServletContextAttribute(ResteasyDeployment.class.getName(), resteasyDeployment)
            .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, websocketDeploymentInfo)
            .addServlet(resteasyServlet).setDeploymentName("RESTEasy Deployment")
            .setClassLoader(BootstrapExtension.class.getClassLoader());


        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        return manager.start();
    }

    public void stop(@Observes BeforeShutdown event) {
        if (UNDERTOW != null)
            UNDERTOW.stop();
    }
}

