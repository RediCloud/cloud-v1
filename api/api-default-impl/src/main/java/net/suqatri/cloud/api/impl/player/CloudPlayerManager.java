package net.suqatri.cloud.api.impl.player;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.player.ICloudPlayer;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RList;
import org.redisson.api.RMap;

import java.util.Collection;
import java.util.UUID;

public class CloudPlayerManager extends RedissonBucketManager<CloudPlayer, ICloudPlayer> implements ICloudPlayerManager {

    private RList<UUID> connectedList;
    private RList<UUID> registeredList;
    private RMap<String, UUID> nameFetcherMap;

    public CloudPlayerManager() {
        super("player", ICloudPlayer.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> {
            this.connectedList = event.getConnection().getClient().getList("players@connected");
            this.registeredList = event.getConnection().getClient().getList("players@registered");
            this.nameFetcherMap = event.getConnection().getClient().getMap("players@nameFetcher");
        });
    }

    @Override
    public IRBucketHolder<ICloudPlayer> getPlayer(String playerName) {
        if(!this.nameFetcherMap.containsKey(playerName)) return null;
        return this.getPlayer(this.nameFetcherMap.get(playerName));
    }

    @Override
    public IRBucketHolder<ICloudPlayer> getPlayer(UUID uniqueId) {
        return this.getPlayer(uniqueId.toString());
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudPlayer>> getPlayerAsync(String playerName) {
        FutureAction<IRBucketHolder<ICloudPlayer>> futureAction = new FutureAction<>();

        this.nameFetcherMap.containsKeyAsync(playerName)
            .whenComplete((contains, throwable) -> {
                if(throwable != null) {
                    futureAction.completeExceptionally(throwable);
                    return;
                }
                this.nameFetcherMap.getAsync(playerName)
                    .whenComplete((uniqueId, throwable1) -> {
                        if(throwable1 != null) {
                            futureAction.completeExceptionally(throwable1);
                            return;
                        }
                        this.getPlayerAsync(uniqueId)
                                .onFailure(futureAction)
                                .onSuccess(futureAction::complete);
                    });
            });

        return futureAction;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudPlayer>> getPlayerAsync(UUID uniqueId) {
        return this.getBucketHolderAsync(uniqueId.toString());
    }

    @Override
    public boolean existsPlayer(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<Boolean> existsPlayerAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }

    @Override
    public IRBucketHolder<ICloudPlayer> createPlayer(ICloudPlayer cloudPlayer) {
        return this.createBucket(cloudPlayer.getUniqueId().toString(), cloudPlayer);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudPlayer>> createPlayerAsync(ICloudPlayer cloudPlayer) {
        return this.createBucketAsync(cloudPlayer.getUniqueId().toString(), cloudPlayer);
    }

    @Override
    public FutureAction<Integer> getRegisteredCount() {
        return new FutureAction<>(this.registeredList.sizeAsync());
    }

    @Override
    public FutureAction<Integer> getOnlineCount() {
        return new FutureAction<>(this.connectedList.sizeAsync());
    }

    @Override
    public FutureAction<UUID> fetchNameAsync(String playerName) {
        FutureAction<UUID> futureAction = new FutureAction<>();

        this.nameFetcherMap.containsKeyAsync(playerName)
            .whenComplete((contains, throwable) -> {
                if(throwable != null) {
                    futureAction.completeExceptionally(throwable);
                    return;
                }
                if(contains) {
                    futureAction.complete(this.nameFetcherMap.get(playerName));
                    return;
                }
                this.nameFetcherMap.getAsync(playerName)
                    .whenComplete((uniqueId, throwable1) -> {
                        if(throwable1 != null) {
                            futureAction.completeExceptionally(throwable1);
                            return;
                        }
                        futureAction.complete(uniqueId);
                        });
            });

        return futureAction;
    }

    @Override
    public UUID fetchName(String playerName) {
        return this.nameFetcherMap.get(playerName);
    }
}