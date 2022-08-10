package dev.redicloud.api.player;

import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudPlayerManager {

    ICloudPlayer getPlayer(String playerName);

    ICloudPlayer getPlayer(UUID uniqueId);

    FutureAction<ICloudPlayer> getPlayerAsync(String playerName);

    FutureAction<ICloudPlayer> getPlayerAsync(UUID uniqueId);

    boolean existsPlayer(UUID uniqueId);
    boolean existsPlayer(String name);

    FutureAction<Boolean> existsPlayerAsync(UUID uniqueId);
    FutureAction<Boolean> existsPlayerAsync(String name);

    ICloudPlayer createPlayer(ICloudPlayer cloudPlayer);
    FutureAction<ICloudPlayer> createPlayerAsync(ICloudPlayer cloudPlayer);

    FutureAction<Integer> getRegisteredCount();

    FutureAction<Integer> getOnlineCount();

    FutureAction<Collection<ICloudPlayer>> getConnectedPlayers();

    FutureAction<UUID> fetchUniqueIdAsync(String playerName);

    UUID fetchUniqueId(String playerName);

    void updateName(UUID uniqueId, String newName, String oldName);

    ICloudService getVerifyService();

}
