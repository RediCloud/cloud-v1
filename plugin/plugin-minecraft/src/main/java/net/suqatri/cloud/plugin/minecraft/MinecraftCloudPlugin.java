package net.suqatri.cloud.plugin.minecraft;

import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.plugin.minecraft.command.BukkitCloudCommandManager;
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
