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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import name.christianbauer.orc3.server.Config;
import name.christianbauer.orc3.server.message.SessionError;
import name.christianbauer.orc3.server.runtime.util.LoggingUtil;

import javax.enterprise.event.Observes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static name.christianbauer.orc3.server.Config.*;

/**
 * General exception handling
 * <p/>
 * In production we want to be INFOrmed of exceptions but only see a stacktrace if FINE
 * debug logging is enabled.
 */
@Provider
public class Exceptions implements ExceptionMapper<Exception>, HttpHandler {

    final private static Logger LOG = Logger.getLogger(Exceptions.class.getName());

    @Context
    protected Request request;

    @Context
    protected UriInfo uriInfo;

    protected HttpHandler nextHandler;

    public static HttpHandler wrap(HttpHandler next) {
        return new Exceptions(next);
    }

    public Exceptions() {
    }

    protected Exceptions(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    // JAX-RS
    @Override
    public Response toResponse(Exception exception) {

        logException(exception, request.getMethod() + " " + uriInfo.getRequestUri());

        if (exception instanceof WebApplicationException) {
            WebApplicationException webApplicationException = (WebApplicationException) exception;
            return webApplicationException.getResponse();
        }

        try {
            if (Boolean.valueOf(System.getProperty(Config.DEVMODE, Config.DEVMODE_DEFAULT))) {
                return Response.serverError().entity(
                    renderTrace(exception)
                ).type(TEXT_PLAIN_TYPE).build();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Couldn't render server error trace response (in DEVMODE)", ex);
        }

        return Response.serverError().entity(renderGeneric()).build();
    }

    // Undertow
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            if (nextHandler == null)
                throw new IllegalStateException("Please use Exceptions::wrap");
            nextHandler.handleRequest(exchange);
        } catch (Exception ex) {

            Exceptions.logException(ex, exchange.getRequestMethod() + " " + exchange.getRequestPath());

            if (exchange.isResponseChannelAvailable()) {
                try {
                    if (Boolean.valueOf(System.getProperty(Config.DEVMODE, Config.DEVMODE_DEFAULT))) {
                        exchange.getResponseSender().send(renderTrace(ex));
                    } else {
                        exchange.getResponseSender().send(renderGeneric());
                    }
                } catch (Exception ex2) {
                    LOG.log(Level.SEVERE, "Couldn't render server error response (in DEVMODE)", ex2);
                }
            }
        }
    }

    // WebSockets
    public void handleError(@Observes SessionError sessionError) {
        logException(sessionError.getThrowable(), "WebSocket Session " + sessionError.getSession().getId());
    }

    public static String renderTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return "500 Server Error\n\n" + sw.toString();
    }

    public static String renderGeneric() {
        return "Request failed due to server error, please check logs and contact the help desk.";
    }

    public static void logException(Throwable throwable, String info) {
        boolean devMode = "true".equals(System.getProperty(DEVMODE, DEVMODE_DEFAULT));
        if (LOG.isLoggable(Level.FINE) || devMode) {
            if (devMode)
                LoggingUtil.logDevModeInfo(LOG, "Logging exception stacktrace");
            LOG.log(
                devMode ? Level.INFO : Level.FINE,
                "Application exception root cause for: " + info, unwrap(throwable)
            );
        } else {
            LOG.log(
                Level.INFO,
                "Application exception occurred processing: " + info + ": " + unwrap(throwable)
            );
        }
    }

    public static Throwable unwrap(Throwable throwable) throws IllegalArgumentException {
        if (throwable == null) {
            throw new IllegalArgumentException("Cannot unwrap null throwable");
        }
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            throwable = current;
        }
        return throwable;
    }
}
