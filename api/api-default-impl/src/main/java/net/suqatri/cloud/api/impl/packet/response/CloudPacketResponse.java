package net.suqatri.cloud.api.impl.packet.response;

import net.suqatri.cloud.api.impl.packet.CloudPacketManager;
import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.packet.ICloudPacketData;
import net.suqatri.cloud.api.packet.ICloudPacketResponse;
import net.suqatri.cloud.commons.function.future.FutureAction;

@Getter
public abstract class CloudPacketResponse extends CloudPacket implements ICloudPacketResponse {

    private String exception;
    private String errorMessage;

    public void setException(Exception exception) {
        this.exception = exception.getClass().getName();
        this.errorMessage = exception.getMessage();
    }

    @Override
    public void receive() {
        if(this.errorMessage == null) return;
        ICloudPacketData packetData = ((CloudPacketManager) CloudAPI.getInstance().getPacketManager()).getWaitingForResponse().get(this.getPacketData().getResponseTargetData().getPacketId());
        FutureAction<ICloudPacketResponse> futureAction = packetData.getResponseAction();
        if(!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()){
            futureAction.completeExceptionally(new Exception("[" + this.exception + "]: " + this.errorMessage));
        }
    }
}
