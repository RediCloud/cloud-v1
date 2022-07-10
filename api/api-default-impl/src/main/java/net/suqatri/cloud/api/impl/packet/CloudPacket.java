package net.suqatri.cloud.api.impl.packet;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.packet.ICloudPacket;

public abstract class CloudPacket implements ICloudPacket {

    private final CloudPacketData packetData = new CloudPacketData();

    @Override
    public CloudPacketData getPacketData() {
        return this.packetData;
    }

    @Override
    public void publish() {
        if(getPacketData().getReceivers().isEmpty()) throw new IllegalStateException("No receivers specified!");
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAsync() {
        if(getPacketData().getReceivers().isEmpty()) throw new IllegalStateException("No receivers specified!");
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }

    @Override
    public void publishAll() {
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAllAsync() {
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }
}
