package dev.redicloud.plugin.minecraft.service;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.CloudServiceManager;
import dev.redicloud.api.service.ICloudService;
import org.bukkit.Bukkit;

public class CloudMinecraftServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        CloudAPI.getInstance().getConsole().trace("Dispatching remote command: " + command);
        CloudAPI.getInstance().getScheduler().runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        return true;
    }

}
