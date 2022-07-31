package net.suqatri.redicloud.api.impl.service.packet.stop;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

public class CloudServiceInitStopPacket extends CloudPacket {

    @Override
    public void receive() {
        CloudAPI.getInstance().shutdown(false);
    }

}
