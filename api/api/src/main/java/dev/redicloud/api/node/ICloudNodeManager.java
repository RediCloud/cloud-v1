package dev.redicloud.api.node;

import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudNodeManager {


    FutureAction<ICloudNode> getNodeAsync(UUID uniqueId);

    ICloudNode getNode(UUID uniqueId);

    FutureAction<ICloudNode> getNodeAsync(String name);

    ICloudNode getNode(String name);

    Collection<ICloudNode> getNodes();

    FutureAction<Collection<ICloudNode>> getNodesAsync();

    boolean existsNode(UUID uniqueId);

    FutureAction<Boolean> existsNodeAsync(UUID uniqueId);

    boolean existsNode(String name);

    FutureAction<Boolean> existsNodeAsync(String name);

    ICloudNode createNode(ICloudNode node);

    FutureAction<ICloudNode> createNodeAsync(ICloudNode node);

}
