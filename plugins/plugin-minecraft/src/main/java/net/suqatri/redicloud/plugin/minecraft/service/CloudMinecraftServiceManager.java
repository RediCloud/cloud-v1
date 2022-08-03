package net.suqatri.redicloud.plugin.minecraft.service;

import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import org.bukkit.Bukkit;

public class CloudMinecraftServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(IRBucketHolder<ICloudService> serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        return true;
    }

}
