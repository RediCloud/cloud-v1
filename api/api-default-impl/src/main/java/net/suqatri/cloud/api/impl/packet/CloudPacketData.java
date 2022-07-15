package net.suqatri.cloud.api.impl.packet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.packet.ICloudPacketData;
import net.suqatri.cloud.api.packet.ICloudPacketResponse;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class CloudPacketData implements ICloudPacketData {

    private final Collection<INetworkComponentInfo> receivers = new ArrayList<>();
    private INetworkComponentInfo sender;
    private boolean allowSenderAsReceiver = false;
    private final UUID packetId = UUID.randomUUID();
    @JsonIgnore
    private FutureAction<ICloudPacketResponse> responseAction;
    @Setter
    private ICloudPacketData responseTargetData;

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
        this.receivers.addAll(Arrays.asList(receivers));
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
    public FutureAction<ICloudPacketResponse> waitForResponse() {
        this.responseAction = new FutureAction<>();
        CloudAPI.getInstance().getPacketManager().registerForResponse(this);
        this.responseAction.orTimeout(30, TimeUnit.SECONDS);
        return responseAction;
    }

    @Override
    public boolean hasReceiver(INetworkComponentInfo receiver) {
        return this.receivers.contains(receiver);
    }
}
