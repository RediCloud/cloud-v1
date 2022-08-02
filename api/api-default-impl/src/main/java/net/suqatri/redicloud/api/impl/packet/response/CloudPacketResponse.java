package net.suqatri.redicloud.api.impl.packet.response;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.impl.packet.CloudPacketManager;
import net.suqatri.redicloud.api.packet.ICloudPacketData;
import net.suqatri.redicloud.api.packet.ICloudPacketResponse;
import net.suqatri.redicloud.commons.function.future.FutureAction;

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
        if (this.errorMessage == null) return;
        ICloudPacketData packetData = ((CloudPacketManager) CloudAPI.getInstance().getPacketManager()).getWaitingForResponse()
                .get(this.getPacketData().getResponseTargetData().getPacketId());
        FutureAction<ICloudPacketResponse> futureAction = packetData.getResponseAction();
        if (!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()) {
            futureAction.completeExceptionally(new Exception("[" + this.exception + "]: " + this.errorMessage));
        }
    }
}
