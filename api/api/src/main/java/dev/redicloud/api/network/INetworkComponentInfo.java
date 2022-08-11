package dev.redicloud.api.network;

import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.Serializable;
import java.util.UUID;

public interface INetworkComponentInfo extends Serializable {

    String getKey();

    UUID getIdentifier();

    NetworkComponentType getType();

    ICloudService getAsService();
    FutureAction<ICloudService> getAsServiceAsync();

    ICloudNode getAsNode();
    FutureAction<ICloudNode> getAsNodeAsync();

}
