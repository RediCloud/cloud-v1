package dev.redicloud.node.service.screen;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.console.IConsoleLine;
import dev.redicloud.api.node.service.screen.IScreenLine;
import dev.redicloud.node.console.ConsoleLine;

@Data
public class ScreenLine implements IScreenLine {

    private final String serviceName;
    private final long time = System.currentTimeMillis();
    private final String line;

    public ScreenLine() {
        this.line = null;
        this.serviceName = null;
    }

    public ScreenLine(String serviceName, String line) {
        this.serviceName = serviceName;
        this.line = line;
    }

    @Override
    public IConsoleLine createConsoleLine() {
        ConsoleLine consoleLine = new ConsoleLine("SCREEN [" + serviceName + "]", line);
        consoleLine.setTime(time);
        consoleLine.setStored(false);
        return consoleLine;
    }

    @Override
    public void print() {
        CloudAPI.getInstance().getConsole().log(createConsoleLine());
    }
}
