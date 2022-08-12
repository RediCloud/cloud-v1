package dev.redicloud.api.impl.network;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.network.INetworkComponentManager;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkComponentManager implements INetworkComponentManager {

    private final ConcurrentHashMap<UUID, INetworkComponentInfo> cachedNodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, INetworkComponentInfo> cachedServices = new ConcurrentHashMap<>();

    public void addCachedNode(ICloudNode node) {
        getComponentInfo(node);
    }

    public void addCachedService(ICloudService service) {
        getComponentInfo(service);
    }

    public void removeCachedNode(ICloudNode node) {
        this.cachedNodes.remove(node.getUniqueId());
    }

    public void removeCachedService(ICloudService service) {
        this.cachedServices.remove(service.getUniqueId());
    }

    public void removeCachedNode(UUID uniqueId) {
        this.cachedNodes.remove(uniqueId);
    }

    public void removeCachedService(UUID uniqueId) {
        this.cachedServices.remove(uniqueId);
    }

    @Override
    public INetworkComponentInfo getComponentInfo(String key) {
        NetworkComponentType type = key.startsWith("node@") ? NetworkComponentType.NODE : NetworkComponentType.SERVICE;
        UUID identifier = UUID.fromString(key.substring(type.name().length() + 1));
        return this.getComponentInfo(type, identifier);
    }

    @Override
    public INetworkComponentInfo getComponentInfo(NetworkComponentType type, UUID identifier) {
        if (type == NetworkComponentType.NODE && this.cachedNodes.containsKey(identifier))
            return this.cachedNodes.get(identifier);
        if (type == NetworkComponentType.SERVICE && this.cachedServices.containsKey(identifier))
            return this.cachedServices.get(identifier);
        return new NetworkComponentInfo(type, identifier);
    }

    @Override
    public INetworkComponentInfo getComponentInfo(ICloudNode node) {
        if (this.cachedNodes.containsKey(node.getUniqueId()))
            return this.cachedNodes.get(node.getUniqueId());
        return new NetworkComponentInfo(NetworkComponentType.NODE, node.getUniqueId());
    }

    @Override
    public INetworkComponentInfo getComponentInfo(ICloudService service) {
        if (this.cachedServices.containsKey(service.getUniqueId()))
            return this.cachedServices.get(service.getUniqueId());
        return new NetworkComponentInfo(NetworkComponentType.SERVICE, service.getUniqueId());
    }

    @Override
    public FutureAction<Collection<INetworkComponentInfo>> getAllComponentInfoAsync() {
        FutureAction<Collection<INetworkComponentInfo>> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(nodes -> {
                    for (ICloudNode node : nodes) {
                        this.cachedNodes.put(node.getUniqueId(),
                                new NetworkComponentInfo(NetworkComponentType.NODE, node.getUniqueId()));
                    }
                    CloudAPI.getInstance().getServiceManager().getServicesAsync()
                            .onFailure(futureAction)
                            .onSuccess(services -> {
                                for (ICloudService service : services) {
                                    this.cachedServices.put(service.getUniqueId(),
                                            new NetworkComponentInfo(NetworkComponentType.SERVICE, service.getUniqueId()));
                                }
                                List<INetworkComponentInfo> infos = new ArrayList<>();
                                infos.addAll(this.cachedServices.values());
                                infos.addAll(this.cachedNodes.values());
                                futureAction.complete(infos);
                            });
                });

        return futureAction;
    }

    @Override
    public Collection<INetworkComponentInfo> getAllComponentInfo() {
        Collection<ICloudNode> nodes = CloudAPI.getInstance().getNodeManager().getNodes();

        for (ICloudNode node : nodes) {
            this.cachedNodes.put(node.getUniqueId(),
                    new NetworkComponentInfo(NetworkComponentType.NODE, node.getUniqueId()));
        }

        Collection<ICloudService> services = CloudAPI.getInstance().getServiceManager().getServices();
        for (ICloudService service : services) {
            this.cachedServices.put(service.getUniqueId(),
                    new NetworkComponentInfo(NetworkComponentType.SERVICE, service.getUniqueId()));
        }

        Collection<INetworkComponentInfo> result = new ArrayList<>();
        result.addAll(this.cachedNodes.values());
        result.addAll(this.cachedServices.values());
        return result;
    }

}
