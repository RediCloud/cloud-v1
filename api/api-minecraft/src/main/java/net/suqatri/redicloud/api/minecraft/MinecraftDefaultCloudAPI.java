package net.suqatri.redicloud.api.minecraft;

import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.minecraft.player.MinecraftPlayerBridge;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.utils.ApplicationType;

public abstract class MinecraftDefaultCloudAPI<T extends RBucketObject> extends CloudDefaultAPIImpl<T> {

    public MinecraftDefaultCloudAPI() {
        super(ApplicationType.SERVICE_MINECRAFT);
    }

    @Override
    public IPlayerBridge createBridge(ICloudPlayer playerHolder) {
        return new MinecraftPlayerBridge(playerHolder);
    }
}
