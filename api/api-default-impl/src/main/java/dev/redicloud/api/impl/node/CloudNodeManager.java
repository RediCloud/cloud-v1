package dev.redicloud.api.impl.node;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.redis.bucket.fetch.RedissonBucketFetchManager;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.node.ICloudNodeManager;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public class CloudNodeManager extends RedissonBucketFetchManager<CloudNode, ICloudNode> implements ICloudNodeManager {

    public CloudNodeManager() {
        super("node", CloudNode.class, "node_names");
    }

    @Override
    public ICloudNode getNode(UUID uniqueId) {
        return this.getBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<ICloudNode> getNodeAsync(String name) {
        FutureAction<ICloudNode> futureAction = new FutureAction<>();

        getFetcherValueAsync(name.toLowerCase())
            .thenAccept(uuid -> getNodeAsync(UUID.fromString(uuid))
                .onFailure(futureAction)
                .onSuccess(futureAction::complete));

        return futureAction;
    }

    @Override
    public ICloudNode getNode(String name) {
        String uuid = getFetcherValue(name);
        return getNode(UUID.fromString(uuid));
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
        return this.getBucketAsync(uniqueId.toString());
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
        return containsKeyInFetcher(name.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> existsNodeAsync(String name) {
        return containsKeyInFetcherAsync(name.toLowerCase());
    }

}
