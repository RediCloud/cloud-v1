package net.suqatri.cloud.api.network;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface INetworkComponentManager {

    INetworkComponentInfo getComponentInfo(String identifier);
    INetworkComponentInfo getComponentInfo(NetworkComponentType type, String identifier);
    INetworkComponentInfo getComponentInfo(ICloudNode node);
    INetworkComponentInfo getComponentInfo(ICloudService service);
    FutureAction<Collection<INetworkComponentInfo>> getAllComponentInfoAsync();
    Collection<INetworkComponentInfo> getAllComponentInfo();

}
