package net.suqatri.cloud.api.network;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface INetworkComponentManager<T extends INetworkComponentInfo> {

    T getComponentInfo(String identifier);
    T getComponentInfo(NetworkComponentType type, String identifier);
    T getComponentInfo(ICloudNode node);
    T getComponentInfo(ICloudService service);
    FutureAction<Collection<T>> getAllComponentInfoAsync();
    Collection<T> getAllComponentInfo();

}
