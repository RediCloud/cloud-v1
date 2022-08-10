package dev.redicloud.api.impl.service.packet.command;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class CloudServiceConsoleCommandPacket extends CloudPacket {

    private UUID serviceId;
    private String command;

    @Override
    public void receive() {
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(this.serviceId)
            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check service existence for service id " + this.serviceId, e))
            .onSuccess(exists -> {
                if(!exists) return;
                CloudAPI.getInstance().getServiceManager().getServiceAsync(this.serviceId)
                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get service for service id " + this.serviceId, e))
                    .onSuccess(service -> {
                        CloudAPI.getInstance().getServiceManager().executeCommand(service, this.command);
                    });
            });
    }
}
