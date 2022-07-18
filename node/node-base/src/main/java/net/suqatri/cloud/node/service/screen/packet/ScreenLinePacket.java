package net.suqatri.cloud.node.service.screen.packet;

import lombok.Data;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.service.screen.ScreenLine;

import java.util.UUID;

@Data
public class ScreenLinePacket extends CloudPacket {

    private ScreenLine screenLine;
    private UUID serviceId;

    @Override
    public void receive() {
        if(NodeLauncher.getInstance().getConsole().getCurrentSetup() != null) return;
        if(!NodeLauncher.getInstance().getScreenManager().isActive(serviceId)) return;
        screenLine.print();
    }
}
