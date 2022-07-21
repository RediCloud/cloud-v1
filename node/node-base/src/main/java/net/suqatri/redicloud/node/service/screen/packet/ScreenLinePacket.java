package net.suqatri.redicloud.node.service.screen.packet;

import lombok.Data;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.node.service.screen.ScreenLine;
import net.suqatri.redicloud.node.NodeLauncher;

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
