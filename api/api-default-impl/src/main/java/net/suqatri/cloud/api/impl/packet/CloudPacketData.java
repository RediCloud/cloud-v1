package net.suqatri.cloud.api.impl.packet;

import lombok.Getter;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.packet.ICloudPacketData;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class CloudPacketData implements ICloudPacketData {

    private Collection<INetworkComponentInfo> receivers = new ArrayList<>();
    private INetworkComponentInfo sender;
    private boolean allowSenderAsReceiver = false;

    public void setSender(INetworkComponentInfo sender) {
        this.sender = sender;
    }

    public CloudPacketData allowSenderAsReceiver() {
        this.allowSenderAsReceiver = true;
        return this;
    }

    @Override
    public boolean isSenderAsReceiverAllowed() {
        return this.allowSenderAsReceiver;
    }

    @Override
    public ICloudPacketData addReceiver(INetworkComponentInfo receiver) {
        this.receivers.add(receiver);
        return this;
    }

    @Override
    public ICloudPacketData addReceivers(INetworkComponentInfo... receivers) {
        for (INetworkComponentInfo receiver : receivers) {
            this.receivers.add(receiver);
        }
        return this;
    }

    @Override
    public ICloudPacketData removeReceiver(INetworkComponentInfo receiver) {
        this.receivers.remove(receiver);
        return this;
    }

    @Override
    public ICloudPacketData removeReceivers(INetworkComponentInfo... receivers) {
        for (INetworkComponentInfo receiver : receivers) {
            this.receivers.remove(receiver);
        }
        return this;
    }

    @Override
    public ICloudPacketData clearReceivers() {
        this.receivers.clear();
        return this;
    }

    @Override
    public boolean hasReceiver(INetworkComponentInfo receiver) {
        return this.receivers.contains(receiver);
    }
}
