package net.suqatri.cloud.node.service.screen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.suqatri.cloud.api.node.service.screen.IScreenLine;
import net.suqatri.cloud.node.console.ConsoleLine;

@Data
public class ScreenLine implements IScreenLine {

    @JsonIgnore
    private final ServiceScreen screen;
    private final long time = System.currentTimeMillis();
    private final String line;

    @Override
    public void print() {
        ConsoleLine consoleLine = new ConsoleLine("SCREEN [" + screen.getService().get().getServiceName() + "]", line);
        consoleLine.setTime(time);
        consoleLine.setStored(false);
        consoleLine.println();
    }
}
