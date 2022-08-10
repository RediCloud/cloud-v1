package dev.redicloud.node.service.screen.packet;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacket;
import dev.redicloud.api.node.service.screen.IServiceScreen;
import dev.redicloud.node.NodeLauncher;

import java.util.UUID;

@Data
public class ScreenDestroyPacket extends CloudPacket {

    private UUID serviceId;

    @Override
    public void receive() {
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(this.serviceId)
                .onSuccess(exists -> {
                    if (!exists) return;
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceId)
                            .onSuccess(serviceHolder -> {
                                IServiceScreen screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                                if (screen == null) return;
                                NodeLauncher.getInstance().getScreenManager().leave(screen);
                            });
                });
    }
}
