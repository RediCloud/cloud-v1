package net.suqatri.redicloud.api.impl;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.configuration.IConfigurationManager;
import net.suqatri.redicloud.api.event.ICloudEventManager;
import net.suqatri.redicloud.api.group.ICloudGroupManager;
import net.suqatri.redicloud.api.impl.configuration.ConfigurationManager;
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
import net.suqatri.redicloud.api.impl.player.RequestPlayerBridge;
import net.suqatri.redicloud.api.impl.player.packet.*;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketDeletePacket;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketUpdatePacket;
import net.suqatri.redicloud.api.impl.service.packet.command.CloudServiceConsoleCommandPacket;
import net.suqatri.redicloud.api.impl.service.packet.stop.CloudServiceInitStopPacket;
import net.suqatri.redicloud.api.impl.utils.CloudProperties;
import net.suqatri.redicloud.api.network.INetworkComponentManager;
import net.suqatri.redicloud.api.node.ICloudNodeManager;
import net.suqatri.redicloud.api.packet.ICloudPacketManager;
import net.suqatri.redicloud.api.packet.PacketChannel;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.ICloudPlayerManager;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.redis.IRedisConnection;
import net.suqatri.redicloud.api.utils.ApplicationType;

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

    public CloudDefaultAPIImpl(ApplicationType type) {
        super(type);
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
