package dev.redicloud.api.minecraft;

import dev.redicloud.api.impl.redis.bucket.RBucketObject;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.minecraft.player.MinecraftPlayerBridge;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.api.utils.ApplicationType;

public abstract class MinecraftDefaultCloudAPI<T extends RBucketObject> extends CloudDefaultAPIImpl<T> {

    public MinecraftDefaultCloudAPI() {
        super(ApplicationType.SERVICE_MINECRAFT);
    }

    @Override
    public IPlayerBridge createBridge(ICloudPlayer player) {
        return new MinecraftPlayerBridge(player);
    }
}
