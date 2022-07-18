package net.suqatri.cloud.api.impl.service.packet.start;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.impl.service.configuration.DefaultServiceStartConfiguration;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;

@Getter @Setter
public class CloudFactoryServiceStartPacket extends CloudPacket {

    private DefaultServiceStartConfiguration configuration;
    private boolean async;

    @Override
    public void receive() {
        CloudFactoryServiceStartResponseCloud response = new CloudFactoryServiceStartResponseCloud();
        if(this.async) {
            CloudAPI.getInstance().getServiceManager().startService(this.configuration)
                    .onFailure(e -> {
                        response.setException(e);
                        this.packetResponseAsync(response);
                    })
                    .onSuccess(holder -> {
                        response.setServiceId(holder.get().getUniqueId());
                        this.packetResponseAsync(response);
                    });
        } else {
            try {
                CloudAPI.getInstance().getServiceManager().startService(this.configuration)
                    .onFailure(e -> {
                        response.setException(e);
                        this.packetResponse(response);
                    })
                    .onSuccess(holder -> {
                        response.setServiceId(holder.get().getUniqueId());
                        this.packetResponseAsync(response);
                    });
            }catch (Exception e){
                response.setException(e);
                this.packetResponseAsync(response);
            }
        }
    }
}
