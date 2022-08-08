package net.suqatri.redicloud.api.impl.configuration;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

public class ConfigurationsReloadPacket extends CloudPacket {

    @Override
    public void receive() {
        CloudAPI.getInstance().getExecutorService().execute(() -> CloudAPI.getInstance().getConfigurationManager().reloadFromDatabase());
    }
}
