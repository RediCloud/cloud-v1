package net.suqatri.cloud.api.impl.packet;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.packet.ICloudPacket;

public abstract class CloudPacket implements ICloudPacket {

    @Override
    public void publish() {
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAsync() {
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }
}
