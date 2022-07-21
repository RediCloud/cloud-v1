package net.suqatri.redicloud.node.service.screen.packet;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.node.NodeLauncher;

import java.util.UUID;

@Data
public class ScreenDestroyPacket extends CloudPacket {

    private UUID serviceId;

    @Override
    public void receive() {
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(this.serviceId)
            .onSuccess(exists -> {
                if(!exists) return;
                CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceId)
                    .onSuccess(serviceHolder -> {
                        IServiceScreen screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                        if(screen == null) return;
                        NodeLauncher.getInstance().getScreenManager().leave(screen);
                    });
            });
    }
}
