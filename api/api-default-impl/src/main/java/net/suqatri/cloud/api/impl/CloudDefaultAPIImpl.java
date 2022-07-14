package net.suqatri.cloud.api.impl;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.impl.event.CloudEventManager;
import net.suqatri.cloud.api.impl.group.CloudGroup;
import net.suqatri.cloud.api.impl.group.CloudGroupManager;
import net.suqatri.cloud.api.impl.listener.node.CloudNodeConnectedListener;
import net.suqatri.cloud.api.impl.listener.node.CloudNodeDisconnectListener;
import net.suqatri.cloud.api.impl.listener.service.CloudServiceStartedListener;
import net.suqatri.cloud.api.impl.listener.service.CloudServiceStoppedListener;
import net.suqatri.cloud.api.impl.network.NetworkComponentInfo;
import net.suqatri.cloud.api.impl.network.NetworkComponentManager;
import net.suqatri.cloud.api.impl.node.CloudNodeManager;
import net.suqatri.cloud.api.impl.packet.CloudPacketManager;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.CloudServiceManager;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.cloud.api.network.INetworkComponentManager;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.ApplicationType;

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
    private final ICloudServiceVersionManager serviceVersionManager;
    private final ExecutorService executorService;

    public CloudDefaultAPIImpl(ApplicationType type) {
        super(type);
        instance = this;
        this.executorService = Executors.newCachedThreadPool();
        this.eventManager = new CloudEventManager();
        this.packetManager = new CloudPacketManager();
        this.networkComponentManager = new NetworkComponentManager();
        this.nodeManager = new CloudNodeManager();
        this.groupManager = new CloudGroupManager();
        this.serviceVersionManager = new CloudServiceVersionManager();
    }

    public void registerInternalListeners(){
        this.eventManager.register(new CloudNodeConnectedListener());
        this.eventManager.register(new CloudNodeDisconnectListener());
        this.eventManager.register(new CloudServiceStartedListener());
        this.eventManager.register(new CloudServiceStoppedListener());
    }

    @Override
    public void initShutdownHook(){
        if(this.isShutdownHookAdded()) return;
        Runtime.getRuntime().addShutdownHook(new Thread("node-shutdown-hook"){
            @Override
            public void run(){
                CloudAPI.getInstance().shutdown(true);
            }
        });
    }

    public abstract IRedisConnection getRedisConnection();
    public abstract void updateApplicationProperties(T object);

}
