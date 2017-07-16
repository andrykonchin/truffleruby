/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogWithoutTruffle {

    private static class RubyLevel extends Level {

        private static final long serialVersionUID = 3759389129096588683L;

        public RubyLevel(String name, Level parent) {
            super(name, parent.intValue(), parent.getResourceBundleName());
        }

    }

    public static final Level PERFORMANCE = new RubyLevel("PERFORMANCE", Level.WARNING);
    public static final Level PATCH = new RubyLevel("PATCH", Level.CONFIG);
    public static final Level[] LEVELS = new Level[]{PERFORMANCE, PATCH};

    public static final Logger LOGGER = createLogger();

    public static class RubyHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            System.err.printf("[ruby] %s %s%n", record.getLevel().getName(), record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    private static Logger createLogger() {
        final Logger logger = Logger.getLogger("org.truffleruby");

        if (LogManager.getLogManager().getProperty("org.truffleruby.handlers") == null) {
            logger.setUseParentHandlers(false);
            logger.addHandler(new RubyHandler());
        }

        return logger;
    }

    private static final Set<String> displayedWarnings = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static final String KWARGS_NOT_OPTIMIZED_YET = "keyword arguments are not yet optimized";

    /**
     * Warn about something that has lower performance than might be expected. Only prints the
     * warning once.
     */
    public static void performanceOnce(String message) {
        if (displayedWarnings.add(message)) {
            LOGGER.log(PERFORMANCE, message);
        }
    }

    public static void warning(String format, Object... args) {
        LOGGER.warning(String.format(format, args));
    }

    public static void info(String format, Object... args) {
        LOGGER.info(String.format(format, args));
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, message);
    }

}
