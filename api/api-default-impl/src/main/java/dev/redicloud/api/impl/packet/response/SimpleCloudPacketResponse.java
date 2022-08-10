package dev.redicloud.api.impl.packet.response;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacketManager;
import dev.redicloud.api.packet.ICloudPacketData;
import dev.redicloud.api.packet.ICloudPacketResponse;
import dev.redicloud.commons.function.future.FutureAction;

@Getter
@Setter
public class SimpleCloudPacketResponse extends CloudPacketResponse {

    @Override
    public void receive() {
        if (this.getException() == null) return;
        ICloudPacketData packetData = ((CloudPacketManager) CloudAPI.getInstance().getPacketManager()).getWaitingForResponse()
                .get(this.getPacketData().getResponseTargetData().getPacketId());
        FutureAction<ICloudPacketResponse> futureAction = packetData.getResponseAction();
        if (!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()) {
            futureAction.completeExceptionally(new Exception("[" + this.getException() + "]: " + this.getErrorMessage()));
        }
    }
}
