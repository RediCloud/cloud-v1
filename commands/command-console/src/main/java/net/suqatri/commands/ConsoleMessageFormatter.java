package net.suqatri.commands;

import java.awt.*;

public class ConsoleMessageFormatter extends MessageFormatter<Color> {

    public ConsoleMessageFormatter(Color... colors) {
        super(colors);
    }

    @Override
    String format(Color color, String message) {
        return color + message;
    }
}
