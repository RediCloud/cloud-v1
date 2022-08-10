package dev.redicloud.api.network;

import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface INetworkComponentManager {

    INetworkComponentInfo getComponentInfo(String identifier);

    INetworkComponentInfo getComponentInfo(NetworkComponentType type, String identifier);

    INetworkComponentInfo getComponentInfo(ICloudNode node);

    INetworkComponentInfo getComponentInfo(ICloudService service);

    FutureAction<Collection<INetworkComponentInfo>> getAllComponentInfoAsync();

    Collection<INetworkComponentInfo> getAllComponentInfo();

}
