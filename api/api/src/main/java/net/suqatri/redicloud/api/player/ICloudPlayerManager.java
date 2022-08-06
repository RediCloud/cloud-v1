package net.suqatri.redicloud.api.player;

import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudPlayerManager {

    ICloudPlayer getPlayer(String playerName);

    ICloudPlayer getPlayer(UUID uniqueId);

    FutureAction<ICloudPlayer> getPlayerAsync(String playerName);

    FutureAction<ICloudPlayer> getPlayerAsync(UUID uniqueId);

    boolean existsPlayer(UUID uniqueId);

    FutureAction<Boolean> existsPlayerAsync(UUID uniqueId);

    ICloudPlayer createPlayer(ICloudPlayer cloudPlayer);
    FutureAction<ICloudPlayer> createPlayerAsync(ICloudPlayer cloudPlayer);

    FutureAction<Integer> getRegisteredCount();

    FutureAction<Integer> getOnlineCount();

    FutureAction<Collection<ICloudPlayer>> getConnectedPlayers();

    FutureAction<UUID> fetchNameAsync(String playerName);

    UUID fetchName(String playerName);

}
