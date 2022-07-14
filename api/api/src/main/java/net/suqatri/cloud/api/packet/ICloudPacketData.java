package net.suqatri.cloud.api.packet;

import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public interface ICloudPacketData extends Serializable{

    Collection<INetworkComponentInfo> getReceivers();
    ICloudPacketData addReceiver(INetworkComponentInfo receiver);
    ICloudPacketData addReceivers(INetworkComponentInfo... receivers);
    ICloudPacketData removeReceiver(INetworkComponentInfo receiver);
    ICloudPacketData removeReceivers(INetworkComponentInfo... receivers);
    ICloudPacketData clearReceivers();

    FutureAction<ICloudPacket> waitForResponse();
    UUID getPacketId();
    UUID getResponseTargetId();
    ICloudPacketData getResponseTargetData();
    void setResponseTargetData(ICloudPacketData packetData);
    FutureAction<ICloudPacket> getResponseAction();

    ICloudPacketData allowSenderAsReceiver();
    boolean isSenderAsReceiverAllowed();

    boolean hasReceiver(INetworkComponentInfo receiver);

    INetworkComponentInfo getSender();

}
