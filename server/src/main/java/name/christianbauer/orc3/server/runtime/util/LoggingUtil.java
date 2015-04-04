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
package name.christianbauer.orc3.server.runtime.util;


import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Simplifies dealing with JUL oddities.
 * <p>
 * This utility class helps you deal with the two most common problems you'll have with
 * java.util.logging (JUL) and friends. The first is loading a default logging configuration without
 * specifying any additional system properties. Put a file called {@code logging.properties}
 * into the root of your classpath and call the static loader methods here.
 * </p>
 * <p>
 * Secondly, this class will automatically instantiate all logging handlers you specified
 * in your logging properties configuration. By default, JUL will search and instantiate handlers
 * using the system classloader. This regularly fails in more complex deployment scenarios, e.g.
 * when building "uber" JARs with delegating classloaders. The correct way to load user-configured
 * handlers is of course with the current thread's context classloader, which is what this utility
 * enables.
 * </p>
 *
 * @author Christian Bauer
 */
@Singleton
public class LoggingUtil {

    public static final String DEFAULT_CONFIG = "logging.properties";

    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

    /**
     * Loads the default logging configuration properties from the classpath.
     * <p>
     * This method will do nothing if the 'java.util.logging.config.file' property is set.
     * </p>
     *
     * @throws java.io.IOException If reading the properties or instantiating handlers failed.
     */
    public static void loadDefaultConfiguration() throws Exception {
        loadDefaultConfiguration(null);
    }

    /**
     * Loads the given logging configuration from the classpath.
     * <p>
     * If the given input stream is null, the default properties will be loaded. This method
     * will do nothing if the 'java.util.logging.config.file' property is set.
     * </p>
     *
     * @param is An optional input stream that overrides the default logging properties.
     * @throws java.io.IOException If reading the properties or instantiating handlers failed.
     */
    public static void loadDefaultConfiguration(InputStream is) throws Exception {
        if (System.getProperty("java.util.logging.config.file") != null) return;

        if (is == null) {
            // Fallback to logging.properties in the root of the classpath, use the
            // customizable classloader on the thread, we don't know in which classloader
            // LoggingUtil is defined.
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG);
        }

        if (is == null) return;

        List<String> handlerNames = new ArrayList();

        LogManager.getLogManager().readConfiguration(
            spliceHandlers(is, handlerNames)
        );

        Handler[] handlers = instantiateHandlers(handlerNames);
        resetRootHandler(handlers);
    }

    /**
     * Loads a handler class with the current context classloader.
     * <p>
     * The JUL manager will load handler classes with the system classloader, causing deployment problems. This
     * method will load a handler class through the current thread's context classloader and instantiate it with
     * the default constructor.
     * </p>
     *
     * @param handlerNames A list of handler class names.
     * @return An array of instantiated handlers.
     * @throws Exception If there was a problem loading or instantiating the handler class.
     */
    public static Handler[] instantiateHandlers(List<String> handlerNames) throws Exception {
        List<Handler> list = new ArrayList();
        for (String handlerName : handlerNames) {
            list.add(
                (Handler) Thread.currentThread().getContextClassLoader().loadClass(handlerName).newInstance()
            );
        }
        return list.toArray(new Handler[list.size()]);
    }

    /**
     * Reads a standard JUL properties stream and removes the 'handlers' property.
     * <p>
     * All handler (class) names are added to the supplied list and an input stream with the same
     * properties, except for the 'handler' properties, is returned.
     * </p>
     *
     * @param is       The properties input stream
     * @param handlers All handler (class) names are added to this list.
     * @return A properties input stream without the 'handlers' property.
     * @throws java.io.IOException When building properties from the input stream failed.
     */
    public static InputStream spliceHandlers(InputStream is, List<String> handlers)
        throws IOException {

        // Load the properties from the original inputstream
        Properties props = new Properties();
        props.load(is);

        // Build new properties
        StringBuilder sb = new StringBuilder();
        List<String> handlersProperties = new ArrayList();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {

            // Hold any 'handlers' property
            if (entry.getKey().equals("handlers")) {
                handlersProperties.add(entry.getValue().toString());
            } else {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }

        // Add all handler classnames (separated by whitespace) to given list
        for (String handlersProperty : handlersProperties) {
            String[] handlerClasses = handlersProperty.trim().split(" ");
            for (String handlerClass : handlerClasses) {
                handlers.add(handlerClass.trim());
            }
        }

        // Return the modified properties without 'handlers' property (note the encoding)
        return new ByteArrayInputStream(sb.toString().getBytes("ISO-8859-1"));
    }

    /**
     * Clears root loggers and adds the given loggers.
     * <p>
     * This method removes all loggers which might have been configured on the JUL root logger
     * handlers (such as the broken default two-line-system-err handler) and adds the given handlers.
     * </p>
     *
     * @param h An array of handlers to use with the root logger.
     */
    public static void resetRootHandler(Handler... h) {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        for (Handler handler : h) {
            if (handler != null)
                LogManager.getLogManager().getLogger("").addHandler(handler);
        }
    }

    public static void logDevModeInfo(Logger LOG, String info) {
        LOG.info("================= DEVMODE enabled: " + info + " =================");
    }
}
