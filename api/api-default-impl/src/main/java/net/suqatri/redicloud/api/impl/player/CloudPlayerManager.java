package net.suqatri.redicloud.api.impl.player;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.ICloudPlayerManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import org.redisson.api.RList;
import org.redisson.api.RMap;

import java.util.Collection;
import java.util.UUID;

public class CloudPlayerManager extends RedissonBucketManager<CloudPlayer, ICloudPlayer> implements ICloudPlayerManager {

    private RList<String> connectedList;
    private RList<String> registeredList;
    private RMap<String, String> nameFetcherMap;

    public CloudPlayerManager() {
        super("player", ICloudPlayer.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> {
            this.connectedList = event.getConnection().getClient().getList("players@connected", getObjectCodec());
            this.registeredList = event.getConnection().getClient().getList("players@registered", getObjectCodec());
            this.nameFetcherMap = event.getConnection().getClient().getMap("players@nameFetcher", getObjectCodec());
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
        this.registeredList.add(cloudPlayer.getUniqueId().toString());
        this.nameFetcherMap.put(cloudPlayer.getName().toLowerCase(), cloudPlayer.getUniqueId().toString());
        return this.createBucket(cloudPlayer.getUniqueId().toString(), cloudPlayer);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudPlayer>> createPlayerAsync(ICloudPlayer cloudPlayer) {
        this.registeredList.addAsync(cloudPlayer.getUniqueId().toString());
        this.nameFetcherMap.putAsync(cloudPlayer.getName().toLowerCase(), cloudPlayer.getUniqueId().toString());
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
    public FutureAction<Collection<IRBucketHolder<ICloudPlayer>>> getConnectedPlayers() {
        FutureAction<Collection<IRBucketHolder<ICloudPlayer>>> futureAction = new FutureAction<>();
        this.nameFetcherMap.readAllValuesAsync()
            .whenComplete((values, throwable) -> {
                if(throwable != null) {
                    futureAction.completeExceptionally(throwable);
                    return;
                }
                FutureActionCollection<UUID, IRBucketHolder<ICloudPlayer>> futureActionCollection = new FutureActionCollection<>();
                for(String value : values) {
                    UUID uniqueId = UUID.fromString(value);
                    futureActionCollection.addToProcess(uniqueId, this.getPlayerAsync(uniqueId));
                }
                futureActionCollection.process()
                    .onFailure(futureAction)
                    .onSuccess(r -> futureAction.complete(r.values()));
            });
        return futureAction;
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
                    this.nameFetcherMap.getAsync(playerName)
                        .whenComplete((uuid, throwable1) -> {
                            if(throwable1 != null) {
                                futureAction.completeExceptionally(throwable1);
                                return;
                            }
                            futureAction.complete(UUID.fromString(uuid));
                    });
                    return;
                }
                this.nameFetcherMap.getAsync(playerName)
                    .whenComplete((uuid, throwable1) -> {
                        if(throwable1 != null) {
                            futureAction.completeExceptionally(throwable1);
                            return;
                        }
                        futureAction.complete(UUID.fromString(uuid));
                        });
            });

        return futureAction;
    }

    @Override
    public UUID fetchName(String playerName) {
        return UUID.fromString(this.nameFetcherMap.get(playerName));
    }

}
