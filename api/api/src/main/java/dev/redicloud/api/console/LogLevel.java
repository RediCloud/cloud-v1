package dev.redicloud.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.util.logging.Level;

@Getter
@AllArgsConstructor
public enum LogLevel {

    TRACE(0, Color.GREEN,  Level.FINEST),
    DEBUG(1, Color.ORANGE, Level.FINE),
    INFO(2, Color.CYAN,  Level.INFO),
    WARN(3, Color.RED.brighter(), Level.WARNING),
    ERROR(4, Color.RED, Level.ALL),
    FATAL(5, Color.RED.darker(), Level.ALL);

    private int id;
    private Color color;
    private Level level;

}
