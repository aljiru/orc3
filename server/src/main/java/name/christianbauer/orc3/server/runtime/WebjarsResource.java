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

import io.undertow.util.MimeMappings;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;

// TODO: This was written in 15 minutes, not sure it's entirely safe loading anything a client wants from classpath
// @Path("/webjars")
public class WebjarsResource {

    private static final Logger LOG = Logger.getLogger(WebjarsResource.class.getName());

    MimeMappings mimeMappings;

    @PostConstruct
    public void init() {
        MimeMappings.Builder mimeBuilder = MimeMappings.builder(true);
        mimeMappings = mimeBuilder.build();
        // TODO: Unify mime mappings
    }

    @Path("{element}")
    public Locator get(@PathParam("element") String element) {
        return new Locator(element);
    }

    public class Locator {

        final String path;

        public Locator(String path) {
            this.path = path;
        }

        @Path("{element}")
        public Object get(@PathParam("element") String element) {
            return new Locator(path + "/" + element);
        }

        @GET
        public Response get() {
            if (path.length() == 0)
                throw new NotFoundException();
            String resource = "META-INF/resources/webjars/" + path;
            LOG.fine("Loading WEBJAR resource: " + resource);
            InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
            if (is == null)
                throw new NotFoundException();

            String fileName = path.substring(path.lastIndexOf("/"));

            String extension = fileName.lastIndexOf(".") != -1
                ? fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(Locale.ROOT)
                : null;

            String mediaType = extension != null
                ? mimeMappings.getMimeType(extension)
                : MediaType.APPLICATION_OCTET_STREAM;

            LOG.fine("Found media type '" + mediaType + "' for file extension: " + extension);

            return Response.ok(is, mediaType).build();
        }
    }

}
