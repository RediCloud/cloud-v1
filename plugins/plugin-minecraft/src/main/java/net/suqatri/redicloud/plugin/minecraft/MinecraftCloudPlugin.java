package net.suqatri.redicloud.plugin.minecraft;

import net.suqatri.redicloud.api.service.ServiceState;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftCloudPlugin extends JavaPlugin {

    private MinecraftCloudAPI cloudAPI;

    @Override
    public void onEnable() {
        cloudAPI = new MinecraftCloudAPI(this);
        cloudAPI.getService().setServiceState(ServiceState.RUNNING_UNDEFINED);
        cloudAPI.getService().update();
    }

    @Override
    public void onDisable() {
        cloudAPI.shutdown(false);
    }
}
