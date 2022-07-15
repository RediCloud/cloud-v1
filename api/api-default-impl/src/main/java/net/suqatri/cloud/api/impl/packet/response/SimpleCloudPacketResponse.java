package net.suqatri.cloud.api.impl.packet.response;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacketManager;
import net.suqatri.cloud.api.packet.ICloudPacket;
import net.suqatri.cloud.api.packet.ICloudPacketData;
import net.suqatri.cloud.api.packet.ICloudPacketResponse;
import net.suqatri.cloud.commons.function.future.FutureAction;

@Getter @Setter
public class SimpleCloudPacketResponse extends CloudPacketResponse {

    @Override
    public void receive() {
        if(this.getException() == null) return;
        ICloudPacketData packetData = ((CloudPacketManager)CloudAPI.getInstance().getPacketManager()).getWaitingForResponse().get(this.getPacketData().getResponseTargetData().getPacketId());
        FutureAction<ICloudPacketResponse> futureAction = packetData.getResponseAction();
        if(!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()){
            futureAction.completeExceptionally(this.getException());
        }
    }
}
