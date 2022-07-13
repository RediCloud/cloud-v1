package net.suqatri.cloud.api.impl.packet;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.network.NetworkComponentInfo;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.packet.ICloudPacket;

import java.util.List;

public abstract class CloudPacket implements ICloudPacket {

    private final CloudPacketData packetData = new CloudPacketData();

    @Override
    public CloudPacketData getPacketData() {
        return this.packetData;
    }

    @Override
    public void publish() {
        if(getPacketData().getReceivers().isEmpty()) throw new IllegalStateException("No receivers specified!");
        for (INetworkComponentInfo receiver : CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfo()) {
            if(receiver.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                    && !this.getPacketData().isAllowSenderAsReceiver()) continue;
            getPacketData().addReceiver(receiver);
        }
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAsync() {
        if(getPacketData().getReceivers().isEmpty()) throw new IllegalStateException("No receivers specified!");
        for (INetworkComponentInfo receiver : CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfo()) {
            if(receiver.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                    && !this.getPacketData().isAllowSenderAsReceiver()) continue;
            getPacketData().addReceiver(receiver);
        }
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }

    @Override
    public void publishAll() {
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        for (INetworkComponentInfo receiver : getPacketData().getReceivers()) {
            if(receiver.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                    && !this.getPacketData().isAllowSenderAsReceiver()) continue;
            getPacketData().addReceiver(receiver);
        }
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAllAsync(NetworkComponentType type) {
        CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfoAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get all component info of type " + type + ". Unable to send " + this.getClass().getName(), e))
                .onSuccess(componentInfos -> {
                    for(INetworkComponentInfo componentInfo : componentInfos) {
                        if(componentInfo.getType() != type) continue;
                        if(componentInfo.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                                && !this.getPacketData().isAllowSenderAsReceiver()) continue;
                        getPacketData().addReceiver(componentInfo);
                    }
                    if(getPacketData().getReceivers().isEmpty()) return;
                    getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
                    CloudAPI.getInstance().getPacketManager().publishAsync(this);
                });
    }

    @Override
    public void publishAllAsync() {
        CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfoAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get all component info. Unable to send " + this.getClass().getName(), e))
                .onSuccess(componentInfos -> {
                    for(INetworkComponentInfo componentInfo : componentInfos) {
                        if(componentInfo.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                                && !this.getPacketData().isAllowSenderAsReceiver()) continue;
                        getPacketData().addReceiver(componentInfo);
                    }
                    if(getPacketData().getReceivers().isEmpty()) return;
                    getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
                    CloudAPI.getInstance().getPacketManager().publishAsync(this);
                });
    }

    @Override
    public void publishAll(NetworkComponentType type) {
        for (INetworkComponentInfo componentInfo : CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfo()) {
            if(componentInfo.getType() != type) continue;
            if(componentInfo.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                    && !this.getPacketData().isAllowSenderAsReceiver()) continue;
            getPacketData().addReceiver(componentInfo);
        }
        if(getPacketData().getReceivers().isEmpty()) return;
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }
}
