package net.suqatri.redicloud.api.impl.player;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.configuration.impl.PlayerConfiguration;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.ICloudPlayerManager;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import org.redisson.api.RList;
import org.redisson.api.RMap;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class CloudPlayerManager extends RedissonBucketManager<CloudPlayer, ICloudPlayer> implements ICloudPlayerManager {

    private RList<String> connectedList;
    private RList<String> registeredList;
    private RMap<String, String> nameFetcherMap;
    @Getter
    private PlayerConfiguration configuration = new PlayerConfiguration();

    public CloudPlayerManager() {
        super("player", CloudPlayer.class);
        CloudAPI.getInstance().getEventManager().registerWithoutBlockWarning(RedisConnectedEvent.class, event -> {
            this.connectedList = event.getConnection().getClient().getList("fetcher:player_connected", getObjectCodec());
            this.registeredList = event.getConnection().getClient().getList("fetcher:player_registered", getObjectCodec());
            this.nameFetcherMap = event.getConnection().getClient().getMap("fetcher:player_nameFetcher", getObjectCodec());
            this.configuration = CloudAPI.getInstance().getConfigurationManager().existsConfiguration(this.configuration.getIdentifier())
                    ? CloudAPI.getInstance().getConfigurationManager().getConfiguration(this.configuration.getIdentifier(), PlayerConfiguration.class)
                    : CloudAPI.getInstance().getConfigurationManager().createConfiguration(this.configuration);
        });
    }

    @Override
    public ICloudPlayer getPlayer(String playerName) {
        if (!this.nameFetcherMap.containsKey(playerName)) return null;
        return this.getPlayer(this.nameFetcherMap.get(playerName));
    }

    @Override
    public ICloudPlayer getPlayer(UUID uniqueId) {
        return this.get(uniqueId.toString());
    }

    @Override
    public FutureAction<ICloudPlayer> getPlayerAsync(String playerName) {
        FutureAction<ICloudPlayer> futureAction = new FutureAction<>();

        this.nameFetcherMap.containsKeyAsync(playerName)
                .whenComplete((contains, throwable) -> {
                    if (throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    this.nameFetcherMap.getAsync(playerName)
                            .whenComplete((uniqueId, throwable1) -> {
                                if (throwable1 != null) {
                                    futureAction.completeExceptionally(throwable1);
                                    return;
                                }
                                this.getAsync(uniqueId)
                                        .onFailure(futureAction::completeExceptionally)
                                        .onSuccess(futureAction::complete);
                            });
                });

        return futureAction;
    }

    @Override
    public FutureAction<ICloudPlayer> getPlayerAsync(UUID uniqueId) {
        return this.getAsync(uniqueId.toString());
    }

    @Override
    public boolean existsPlayer(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public boolean existsPlayer(String name) {
        return this.nameFetcherMap.containsKey(name.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> existsPlayerAsync(String name) {
        return new FutureAction<>(this.nameFetcherMap.containsKeyAsync(name.toLowerCase()));
    }

    @Override
    public FutureAction<Boolean> existsPlayerAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }

    @Override
    public ICloudPlayer createPlayer(ICloudPlayer cloudPlayer) {
        this.registeredList.add(cloudPlayer.getUniqueId().toString());
        this.nameFetcherMap.put(cloudPlayer.getName().toLowerCase(), cloudPlayer.getUniqueId().toString());
        return this.createBucket(cloudPlayer.getUniqueId().toString(), cloudPlayer);
    }

    @Override
    public FutureAction<ICloudPlayer> createPlayerAsync(ICloudPlayer cloudPlayer) {
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
    public FutureAction<Collection<ICloudPlayer>> getConnectedPlayers() {
        FutureAction<Collection<ICloudPlayer>> futureAction = new FutureAction<>();
        this.nameFetcherMap.readAllValuesAsync()
                .whenComplete((values, throwable) -> {
                    if (throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    FutureActionCollection<UUID, ICloudPlayer> futureActionCollection = new FutureActionCollection<>();
                    for (String value : values) {
                        UUID uniqueId = UUID.fromString(value);
                        futureActionCollection.addToProcess(uniqueId, this.getPlayerAsync(uniqueId));
                    }
                    futureActionCollection.process()
                            .onFailure(futureAction)
                            .onSuccess(r -> futureAction.complete(r.values()
                                    .parallelStream()
                                    .filter(ICloudPlayer::isConnected)
                                    .collect(Collectors.toList())));
                });
        return futureAction;
    }

    @Override
    public FutureAction<UUID> fetchUniqueIdAsync(String playerName) {
        FutureAction<UUID> futureAction = new FutureAction<>();

        this.nameFetcherMap.containsKeyAsync(playerName)
                .whenComplete((contains, throwable) -> {
                    if (throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    if (contains) {
                        this.nameFetcherMap.getAsync(playerName)
                                .whenComplete((uuid, throwable1) -> {
                                    if (throwable1 != null) {
                                        futureAction.completeExceptionally(throwable1);
                                        return;
                                    }
                                    futureAction.complete(UUID.fromString(uuid));
                                });
                        return;
                    }
                    this.nameFetcherMap.getAsync(playerName)
                            .whenComplete((uuid, throwable1) -> {
                                if (throwable1 != null) {
                                    futureAction.completeExceptionally(throwable1);
                                    return;
                                }
                                futureAction.complete(UUID.fromString(uuid));
                            });
                });

        return futureAction;
    }

    @Override
    public UUID fetchUniqueId(String playerName) {
        return UUID.fromString(this.nameFetcherMap.get(playerName));
    }

    @Override
    public void updateName(UUID uniqueId, String newName, String oldName) {
        this.nameFetcherMap.remove(oldName);
        this.nameFetcherMap.put(uniqueId.toString(), newName);
    }

    @Override
    public ICloudService getVerifyService() {
        for (ICloudService service : CloudAPI.getInstance().getServiceManager().getServices()) {
            if(!service.isGroupBased()) continue;
            if(service.getGroupName().equals("Verify")) continue;
            if(service.getServiceState() != ServiceState.RUNNING_UNDEFINED) continue;
            if(service.getEnvironment() != ServiceEnvironment.LIMBO) continue;
            return service;
        }
        return null;
    }

}
