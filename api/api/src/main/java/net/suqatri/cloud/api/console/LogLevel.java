package net.suqatri.cloud.api.console;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogLevel {

    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5);

    private int id;
}
