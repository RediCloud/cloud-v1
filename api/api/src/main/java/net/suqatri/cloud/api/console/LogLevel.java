package net.suqatri.cloud.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.util.logging.Level;

@Getter
@AllArgsConstructor
public enum LogLevel {

    TRACE(0, Color.GREEN),
    DEBUG(1, Color.ORANGE),
    INFO(2, Color.CYAN),
    WARN(3, Color.RED.brighter()),
    ERROR(4, Color.RED),
    FATAL(5, Color.RED.darker());

    private int id;
    private Color color;
}
