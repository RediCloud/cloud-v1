package net.suqatri.cloud.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@Getter
@AllArgsConstructor
public enum LogLevel {

    DEBUG(1, Color.ORANGE),
    INFO(2, Color.CYAN),
    WARN(3, Color.RED.brighter()),
    ERROR(4, Color.RED),
    FATAL(5, Color.RED.darker());

    private int id;
    private Color color;
}
