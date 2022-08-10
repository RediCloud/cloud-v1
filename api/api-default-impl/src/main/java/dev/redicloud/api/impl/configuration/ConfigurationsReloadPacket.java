package dev.redicloud.api.impl.configuration;

import dev.redicloud.api.impl.packet.CloudPacket;
import dev.redicloud.api.CloudAPI;

public class ConfigurationsReloadPacket extends CloudPacket {

    @Override
    public void receive() {
        CloudAPI.getInstance().getExecutorService().execute(() -> CloudAPI.getInstance().getConfigurationManager().reloadFromDatabase());
    }
}
