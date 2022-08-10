package dev.redicloud.api.impl.service.packet.stop;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacket;

public class CloudServiceInitStopPacket extends CloudPacket {

    @Override
    public void receive() {
        CloudAPI.getInstance().shutdown(false);
    }

}
