package net.suqatri.redicloud.api.impl.service.packet.command;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

import java.util.UUID;

@Getter @Setter
public class CloudServiceConsoleCommandPacket extends CloudPacket {

    private UUID serviceId;
    private String command;

    @Override
    public void receive() {

    }
}
