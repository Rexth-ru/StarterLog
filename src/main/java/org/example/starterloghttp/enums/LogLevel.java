package org.example.starterloghttp.enums;

import org.slf4j.event.Level;

public enum LogLevel {
    INFO,
    DEBUG,
    WARN,
    ERROR,
    TRACE;

    public static Level toSlf4jLevel(LogLevel level) {
        return switch (level) {
            case INFO -> Level.INFO;
            case DEBUG -> Level.DEBUG;
            case WARN -> Level.WARN;
            case ERROR -> Level.ERROR;
            case TRACE -> Level.TRACE;
        };
    }
}
