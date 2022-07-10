package net.suqatri.cloud.api.packet;

import net.suqatri.cloud.api.network.INetworkComponentInfo;

import java.io.Serializable;
import java.util.Collection;

public interface ICloudPacketData extends Serializable{

    Collection<INetworkComponentInfo> getReceivers();
    ICloudPacketData addReceiver(INetworkComponentInfo receiver);
    ICloudPacketData addReceivers(INetworkComponentInfo... receivers);
    ICloudPacketData removeReceiver(INetworkComponentInfo receiver);
    ICloudPacketData removeReceivers(INetworkComponentInfo... receivers);
    ICloudPacketData clearReceivers();

    ICloudPacketData allowSenderAsReceiver();
    boolean isSenderAsReceiverAllowed();

    boolean hasReceiver(INetworkComponentInfo receiver);

    INetworkComponentInfo getSender();

}
