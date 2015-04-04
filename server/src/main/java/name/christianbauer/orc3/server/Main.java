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

import name.christianbauer.orc3.server.runtime.util.LoggingUtil;
import org.jboss.weld.environment.se.StartMain;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

public class Main implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    static StartMain START_MAIN;

    static {
        try {
            LoggingUtil.loadDefaultConfiguration();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        LOG.info("Starting runtime container...");

        Thread.setDefaultUncaughtExceptionHandler(new Main());

        START_MAIN = new StartMain(args);
        START_MAIN.go();

        LOG.info("Runtime startup took: " + ManagementFactory.getRuntimeMXBean().getUptime() + "ms");
    }

    public static void shutdownNow() {
        LOG.info("Stopping runtime container...");
        START_MAIN.shutdownNow();
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable throwable) {
        System.err.println("In thread '" + thread + "', uncaught exception: " + throwable);
        throwable.printStackTrace(System.err);
    }

}
