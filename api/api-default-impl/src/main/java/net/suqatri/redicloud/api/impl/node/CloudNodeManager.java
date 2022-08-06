package net.suqatri.redicloud.api.impl.node;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.ICloudNodeManager;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class CloudNodeManager extends RedissonBucketManager<CloudNode, ICloudNode> implements ICloudNodeManager {

    public CloudNodeManager() {
        super("node", CloudNode.class);
    }

    @Override
    public ICloudNode getNode(UUID uniqueId) {
        return this.get(uniqueId.toString());
    }

    @Override
    public FutureAction<ICloudNode> getNodeAsync(String name) {
        FutureAction<ICloudNode> futureAction = new FutureAction<>();

        getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(nodes -> {
                    Optional<ICloudNode> optional = nodes
                            .parallelStream()
                            .filter(node -> node.getName().equalsIgnoreCase(name))
                            .findFirst();
                    if (optional.isPresent()) {
                        futureAction.complete(optional.get());
                    } else {
                        futureAction.completeExceptionally(new IllegalArgumentException("Node not found"));
                    }
                });

        return futureAction;
    }

    @Override
    public ICloudNode getNode(String name) {
        return getNodes().parallelStream().filter(node -> node.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public Collection<ICloudNode> getNodes() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<ICloudNode>> getNodesAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public FutureAction<ICloudNode> getNodeAsync(UUID uniqueId) {
        return this.getAsync(uniqueId.toString());
    }

    public void shutdownCluster() {
        for (ICloudNode node : getNodes()) {
            node.shutdown();
        }
    }

    public void shutdownClusterAsync() {
        getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while shutting down cluster", e))
                .onSuccess(nodes -> {
                    for (ICloudNode node : nodes) {
                        node.shutdown();
                    }
                });
    }

    @Override
    public ICloudNode createNode(ICloudNode node) {
        return this.createBucket(node.getUniqueId().toString(), node);
    }

    @Override
    public FutureAction<ICloudNode> createNodeAsync(ICloudNode node) {
        return this.createBucketAsync(node.getUniqueId().toString(), node);
    }

    public boolean existsNode(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    public FutureAction<Boolean> existsNodeAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }

    @Override
    public boolean existsNode(String name) {
        return getNode(name) != null;
    }

    @Override
    public FutureAction<Boolean> existsNodeAsync(String name) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        getNodeAsync(name)
                .onFailure(e -> {
                    if (e instanceof NullPointerException) {
                        futureAction.complete(false);
                    } else {
                        futureAction.completeExceptionally(e);
                    }
                })
                .onSuccess(node -> futureAction.complete(true));

        return futureAction;
    }

}
