package dev.redicloud.api.network;

import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface INetworkComponentManager {

    INetworkComponentInfo getComponentInfo(String key);

    INetworkComponentInfo getComponentInfo(NetworkComponentType type, UUID identifier);

    INetworkComponentInfo getComponentInfo(ICloudNode node);

    INetworkComponentInfo getComponentInfo(ICloudService service);

    FutureAction<Collection<INetworkComponentInfo>> getAllComponentInfoAsync();

    Collection<INetworkComponentInfo> getAllComponentInfo();

}
