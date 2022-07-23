package net.suqatri.redicloud.api.impl;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.ICloudEventManager;
import net.suqatri.redicloud.api.group.ICloudGroupManager;
import net.suqatri.redicloud.api.impl.event.CloudEventManager;
import net.suqatri.redicloud.api.impl.group.CloudGroupManager;
import net.suqatri.redicloud.api.impl.listener.node.CloudNodeConnectedListener;
import net.suqatri.redicloud.api.impl.listener.node.CloudNodeDisconnectListener;
import net.suqatri.redicloud.api.impl.listener.service.CloudServiceStartedListener;
import net.suqatri.redicloud.api.impl.listener.service.CloudServiceStoppedListener;
import net.suqatri.redicloud.api.impl.network.NetworkComponentManager;
import net.suqatri.redicloud.api.impl.node.CloudNodeManager;
import net.suqatri.redicloud.api.impl.packet.CloudPacketManager;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketDeletePacket;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketUpdatePacket;
import net.suqatri.redicloud.api.network.INetworkComponentManager;
import net.suqatri.redicloud.api.node.ICloudNodeManager;
import net.suqatri.redicloud.api.packet.ICloudPacketManager;
import net.suqatri.redicloud.api.player.ICloudPlayerManager;
import net.suqatri.redicloud.api.redis.IRedisConnection;
import net.suqatri.redicloud.api.utils.ApplicationType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public abstract class CloudDefaultAPIImpl<T extends RBucketObject> extends CloudAPI {

    @Getter
    private static CloudDefaultAPIImpl instance;

    private final ICloudEventManager eventManager;
    private final ICloudPacketManager packetManager;
    private final INetworkComponentManager networkComponentManager;
    private final ICloudNodeManager nodeManager;
    private final ICloudGroupManager groupManager;
    private final ExecutorService executorService;
    private final ICloudPlayerManager playerManager;

    public CloudDefaultAPIImpl(ApplicationType type) {
        super(type);
        instance = this;
        this.executorService = Executors.newCachedThreadPool();
        this.eventManager = new CloudEventManager();
        this.packetManager = new CloudPacketManager();
        this.networkComponentManager = new NetworkComponentManager();
        this.nodeManager = new CloudNodeManager();
        this.groupManager = new CloudGroupManager();
        this.playerManager = new CloudPlayerManager();
    }

    public void registerInternalListeners() {
        this.eventManager.register(new CloudNodeConnectedListener());
        this.eventManager.register(new CloudNodeDisconnectListener());
        this.eventManager.register(new CloudServiceStartedListener());
        this.eventManager.register(new CloudServiceStoppedListener());
    }

    public void registerInternalPackets() {
        CloudAPI.getInstance().getPacketManager().registerPacket(BucketUpdatePacket.class);
        CloudAPI.getInstance().getPacketManager().registerPacket(BucketDeletePacket.class);
    }

    @Override
    public void initShutdownHook() {
        if (this.isShutdownHookAdded()) return;
        Runtime.getRuntime().addShutdownHook(new Thread("node-shutdown-hook") {
            @Override
            public void run() {
                CloudAPI.getInstance().shutdown(true);
            }
        });
    }

    public abstract IRedisConnection getRedisConnection();

    public abstract void updateApplicationProperties(T object);

}
