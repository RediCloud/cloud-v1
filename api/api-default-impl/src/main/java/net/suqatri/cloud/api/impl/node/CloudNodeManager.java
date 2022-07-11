package net.suqatri.cloud.api.impl.node;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;

import java.util.*;

public class CloudNodeManager extends RedissonBucketManager<CloudNode> implements ICloudNodeManager {

    public CloudNodeManager() {
        super("node", CloudNode.class);
    }

    @Override
    public IRBucketHolder<CloudNode> getNode(UUID uniqueId) {
        return this.getBucketHolder(uniqueId.toString());
    }

    @Override
    public FutureAction<IRBucketHolder<CloudNode>> getNodeAsync(String name) {
        FutureAction<IRBucketHolder<CloudNode>> futureAction = new FutureAction<>();

        getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(nodes -> {
                    Optional<IRBucketHolder<CloudNode>> optional = nodes
                            .parallelStream()
                            .filter(node -> node.get().getName().equalsIgnoreCase(name))
                            .findFirst();
                    if(optional.isPresent()) {
                        futureAction.complete(optional.get());
                    } else {
                        futureAction.completeExceptionally(new IllegalArgumentException("Node not found"));
                    }
                });

        return futureAction;
    }

    @Override
    public IRBucketHolder<CloudNode> getNode(String name) {
        return getNodes().parallelStream().filter(node -> node.get().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public Collection<IRBucketHolder<CloudNode>> getNodes() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<CloudNode>>> getNodesAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public FutureAction<IRBucketHolder<CloudNode>> getNodeAsync(UUID uniqueId){
        return this.getBucketHolderAsync(uniqueId.toString());
    }

    public void shutdownCluster(){
        for (IRBucketHolder<CloudNode> node : getNodes()) {
            node.get().shutdown();
        }
    }

    public void shutdownClusterAsync(){
        getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while shutting down cluster", e))
                .onSuccess(nodes -> {
                    for (IRBucketHolder<CloudNode> node : nodes) {
                        node.get().shutdown();
                    }
                });
    }

    public IRBucketHolder<CloudNode> createNode(CloudNode node) {
        return this.createBucket(node.getUniqueId().toString(), node);
    }

    public FutureAction<IRBucketHolder<CloudNode>> createNodeAsync(CloudNode node) {
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
                    if(e instanceof NullPointerException){
                        futureAction.complete(false);
                    }else{
                        futureAction.completeExceptionally(e);
                    }
                })
                .onSuccess(node -> futureAction.complete(true));

        return futureAction;
    }

}
