package dev.redicloud.api.impl;

import dev.redicloud.api.impl.event.CloudEventManager;
import dev.redicloud.api.impl.group.CloudGroupManager;
import dev.redicloud.api.impl.listener.node.CloudNodeConnectedListener;
import dev.redicloud.api.impl.listener.node.CloudNodeDisconnectListener;
import dev.redicloud.api.impl.listener.service.CloudServiceStartedListener;
import dev.redicloud.api.impl.listener.service.CloudServiceStoppedListener;
import dev.redicloud.api.impl.network.NetworkComponentManager;
import dev.redicloud.api.impl.node.CloudNodeManager;
import dev.redicloud.api.impl.packet.CloudPacketManager;
import dev.redicloud.api.impl.player.CloudPlayerManager;
import dev.redicloud.api.impl.player.RequestPlayerBridge;
import dev.redicloud.api.impl.player.packet.*;
import dev.redicloud.api.impl.redis.bucket.RBucketObject;
import dev.redicloud.api.impl.redis.bucket.packet.BucketDeletePacket;
import dev.redicloud.api.impl.redis.bucket.packet.BucketUpdatePacket;
import dev.redicloud.api.impl.service.packet.stop.CloudServiceInitStopPacket;
import dev.redicloud.api.impl.utils.CloudProperties;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.dependency.DependencyLoader;
import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.configuration.IConfigurationManager;
import dev.redicloud.api.event.ICloudEventManager;
import dev.redicloud.api.group.ICloudGroupManager;
import dev.redicloud.api.impl.configuration.ConfigurationManager;
import dev.redicloud.api.impl.service.packet.command.CloudServiceConsoleCommandPacket;
import dev.redicloud.api.network.INetworkComponentManager;
import dev.redicloud.api.node.ICloudNodeManager;
import dev.redicloud.api.packet.ICloudPacketManager;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.ICloudPlayerManager;
import dev.redicloud.api.redis.IRedisConnection;
import dev.redicloud.api.utils.ApplicationType;

import java.io.IOException;
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
    private final IConfigurationManager configurationManager;
    private CloudProperties properties;

    public CloudDefaultAPIImpl(ApplicationType type, DependencyLoader dependencyLoader) {
        super(type, dependencyLoader);
        instance = this;
        this.loadProperties();
        this.executorService = Executors.newCachedThreadPool();
        this.eventManager = new CloudEventManager();
        this.packetManager = new CloudPacketManager();
        this.networkComponentManager = new NetworkComponentManager();
        this.nodeManager = new CloudNodeManager();
        this.groupManager = new CloudGroupManager();
        this.playerManager = new CloudPlayerManager();
        this.configurationManager = new ConfigurationManager();
    }

    @Override
    public IPlayerBridge createBridge(ICloudPlayer player) {
        return new RequestPlayerBridge(player);
    }

    private void loadProperties(){
        try {
            this.properties = new CloudProperties(this.getClass().getClassLoader().getResourceAsStream("redicloud.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerInternalListeners() {
        this.eventManager.register(new CloudNodeConnectedListener());
        this.eventManager.register(new CloudNodeDisconnectListener());
        this.eventManager.register(new CloudServiceStartedListener());
        this.eventManager.register(new CloudServiceStoppedListener());
    }

    public void registerInternalPackets() {
        CloudAPI.getInstance().getPacketManager().registerPacket(BucketUpdatePacket.class, PacketChannel.GLOBAL);
        CloudAPI.getInstance().getPacketManager().registerPacket(BucketDeletePacket.class, PacketChannel.GLOBAL);

        CloudAPI.getInstance().getPacketManager().registerPacket(CloudServiceConsoleCommandPacket.class, PacketChannel.GLOBAL);

        CloudAPI.getInstance().getPacketManager().registerPacket(CloudServiceInitStopPacket.class, PacketChannel.GLOBAL);

        CloudAPI.getInstance().getPacketManager().registerPacket(CloudBridgeMessagePacket.class, PacketChannel.GLOBAL);
        CloudAPI.getInstance().getPacketManager().registerPacket(CloudBridgeTitlePacket.class, PacketChannel.GLOBAL);
        CloudAPI.getInstance().getPacketManager().registerPacket(CloudBridgeActionbarPacket.class, PacketChannel.GLOBAL);
        CloudAPI.getInstance().getPacketManager().registerPacket(CloudBridgeConnectServicePacket.class, PacketChannel.GLOBAL);
        CloudAPI.getInstance().getPacketManager().registerPacket(CloudBridgeDisconnectPacket.class, PacketChannel.GLOBAL);
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
