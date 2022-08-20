package dev.redicloud.api.impl.player;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.configuration.impl.PlayerConfiguration;
import dev.redicloud.api.impl.redis.bucket.fetch.RedissonBucketFetchManager;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.ICloudPlayerManager;
import dev.redicloud.api.redis.event.RedisConnectedEvent;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.commons.function.future.FutureActionCollection;
import lombok.Getter;
import org.redisson.api.RList;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class CloudPlayerManager extends RedissonBucketFetchManager<CloudPlayer, ICloudPlayer> implements ICloudPlayerManager {

    private RList<String> connectedList;
    private RList<String> registeredList;
    @Getter
    private PlayerConfiguration configuration = new PlayerConfiguration();

    public CloudPlayerManager() {
        super("player", CloudPlayer.class, "player_nameFetcher");
        CloudAPI.getInstance().getEventManager().registerWithoutBlockWarning(RedisConnectedEvent.class, event -> {
            this.connectedList = event.getConnection().getClient().getList("fetcher:player_connected", getObjectCodec());
            this.registeredList = event.getConnection().getClient().getList("fetcher:player_registered", getObjectCodec());
            this.configuration = CloudAPI.getInstance().getConfigurationManager().existsConfiguration(this.configuration.getIdentifier())
                    ? CloudAPI.getInstance().getConfigurationManager().getConfiguration(this.configuration.getIdentifier(), PlayerConfiguration.class)
                    : CloudAPI.getInstance().getConfigurationManager().createConfiguration(this.configuration);
        });
    }

    @Override
    public ICloudPlayer getPlayer(String playerName) {
        return this.getPlayer(this.getFetcherValue(playerName.toLowerCase()));
    }

    @Override
    public ICloudPlayer getPlayer(UUID uniqueId) {
        return this.getBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<ICloudPlayer> getPlayerAsync(String playerName) {
        FutureAction<ICloudPlayer> futureAction = new FutureAction<>();

        this.containsKeyInFetcherAsync(playerName.toLowerCase())
                .whenComplete((contains, throwable) -> {
                    if (throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    this.getFetcherValueAsync(playerName.toLowerCase())
                            .whenComplete((uniqueId, throwable1) -> {
                                if (throwable1 != null) {
                                    futureAction.completeExceptionally(throwable1);
                                    return;
                                }
                                this.getBucketAsync(uniqueId)
                                        .onFailure(futureAction::completeExceptionally)
                                        .onSuccess(futureAction::complete);
                            });
                });

        return futureAction;
    }

    @Override
    public FutureAction<ICloudPlayer> getPlayerAsync(UUID uniqueId) {
        return this.getBucketAsync(uniqueId.toString());
    }

    @Override
    public boolean existsPlayer(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public boolean existsPlayer(String name) {
        return this.containsKeyInFetcher(name.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> existsPlayerAsync(String name) {
        return new FutureAction<>(this.containsKeyInFetcherAsync(name.toLowerCase()));
    }

    @Override
    public FutureAction<Boolean> existsPlayerAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }

    @Override
    public ICloudPlayer createPlayer(ICloudPlayer cloudPlayer) {
        this.registeredList.add(cloudPlayer.getUniqueId().toString());
        return this.createBucket(cloudPlayer.getUniqueId().toString(), cloudPlayer);
    }

    @Override
    public FutureAction<ICloudPlayer> createPlayerAsync(ICloudPlayer cloudPlayer) {
        this.registeredList.addAsync(cloudPlayer.getUniqueId().toString());
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
        this.readAllFetcherKeysAsync()
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

        this.containsKeyInFetcherAsync(playerName.toLowerCase())
            .whenComplete((contains, throwable) -> {
                if (throwable != null) {
                    futureAction.completeExceptionally(throwable);
                    return;
                }
                this.getFetcherValueAsync(playerName.toLowerCase())
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
        return UUID.fromString(this.getFetcherValue(playerName.toLowerCase()));
    }

    @Override
    public void updateName(UUID uniqueId, String newName, String oldName) {
        this.removeFromFetcher(oldName.toLowerCase());
        this.putInFetcher(newName.toLowerCase(), uniqueId.toString());
    }

    @Override
    public ICloudService getVerifyService() {
        for (ICloudService service : CloudAPI.getInstance().getServiceManager().getServices()) {
            if(!service.isGroupBased()) continue;
            if(!service.getGroupName().equals("Verify")) continue;
            if(service.getServiceState() != ServiceState.RUNNING_UNDEFINED) continue;
            if(service.getEnvironment() != ServiceEnvironment.LIMBO) continue;
            return service;
        }
        return null;
    }

}
