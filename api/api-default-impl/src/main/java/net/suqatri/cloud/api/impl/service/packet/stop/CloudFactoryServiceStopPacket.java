package net.suqatri.cloud.api.impl.service.packet.stop;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;

import java.util.UUID;

@Getter @Setter
public class CloudFactoryServiceStopPacket extends CloudPacket {

    private UUID serviceId;
    private boolean async;
    private boolean force;

    @Override
    public void receive() {
        if(this.async) {
            CloudAPI.getInstance().getServiceManager().stopServiceAsync(this.serviceId, this.force)
                    .onFailure(this::simplePacketResponseAsync)
                    .onSuccess(holder -> this.simplePacketResponseAsync());
        } else {
            try {
                CloudAPI.getInstance().getServiceManager().stopService(this.serviceId, this.force);
                this.simplePacketResponseAsync();
            }catch (Exception e){
                this.simplePacketResponseAsync(e);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
