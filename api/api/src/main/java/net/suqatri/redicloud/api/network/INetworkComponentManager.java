package net.suqatri.redicloud.api.network;

import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface INetworkComponentManager {

    INetworkComponentInfo getComponentInfo(String identifier);
    INetworkComponentInfo getComponentInfo(NetworkComponentType type, String identifier);
    INetworkComponentInfo getComponentInfo(ICloudNode node);
    INetworkComponentInfo getComponentInfo(ICloudService service);
    FutureAction<Collection<INetworkComponentInfo>> getAllComponentInfoAsync();
    Collection<INetworkComponentInfo> getAllComponentInfo();

}
