package net.suqatri.redicloud.api.impl.packet.response;

import net.suqatri.redicloud.api.impl.packet.CloudPacketManager;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.packet.ICloudPacketData;
import net.suqatri.redicloud.api.packet.ICloudPacketResponse;
import net.suqatri.redicloud.commons.function.future.FutureAction;

@Getter @Setter
public class SimpleCloudPacketResponse extends CloudPacketResponse {

    @Override
    public void receive() {
        if(this.getException() == null) return;
        ICloudPacketData packetData = ((CloudPacketManager) CloudAPI.getInstance().getPacketManager()).getWaitingForResponse().get(this.getPacketData().getResponseTargetData().getPacketId());
        FutureAction<ICloudPacketResponse> futureAction = packetData.getResponseAction();
        if(!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()){
            futureAction.completeExceptionally(new Exception("[" + this.getException() + "]: " + this.getErrorMessage()));
        }
    }
}
