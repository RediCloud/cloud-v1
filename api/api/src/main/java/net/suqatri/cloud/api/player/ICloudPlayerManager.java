package net.suqatri.cloud.api.player;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudPlayerManager {

    IRBucketHolder<ICloudPlayer> getPlayer(String playerName);
    IRBucketHolder<ICloudPlayer> getPlayer(UUID uniqueId);

    FutureAction<IRBucketHolder<ICloudPlayer>> getPlayerAsync(String playerName);
    FutureAction<IRBucketHolder<ICloudPlayer>> getPlayerAsync(UUID uniqueId);

    boolean existsPlayer(UUID uniqueId);
    FutureAction<Boolean> existsPlayerAsync(UUID uniqueId);

    IRBucketHolder<ICloudPlayer> createPlayer(ICloudPlayer cloudPlayer);
    FutureAction<IRBucketHolder<ICloudPlayer>> createPlayerAsync(ICloudPlayer cloudPlayer);

    FutureAction<Integer> getRegisteredCount();
    FutureAction<Integer> getOnlineCount();

    FutureAction<Collection<IRBucketHolder<ICloudPlayer>>> getConnectedPlayers();

    FutureAction<UUID> fetchNameAsync(String playerName);
    UUID fetchName(String playerName);

}
