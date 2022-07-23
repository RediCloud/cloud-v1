package net.suqatri.redicloud.node.service.screen;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.console.IConsoleLine;
import net.suqatri.redicloud.api.node.service.screen.IScreenLine;
import net.suqatri.redicloud.node.console.ConsoleLine;

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
