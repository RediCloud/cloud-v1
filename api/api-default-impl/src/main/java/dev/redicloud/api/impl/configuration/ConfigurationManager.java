package dev.redicloud.api.impl.configuration;

import dev.redicloud.api.configuration.ConfigurationReloadEvent;
import dev.redicloud.api.configuration.IConfiguration;
import dev.redicloud.api.configuration.IConfigurationManager;
import dev.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.api.redis.event.RedisConnectedEvent;
import dev.redicloud.api.CloudAPI;

public class ConfigurationManager extends RedissonBucketManager<Configuration, IConfiguration> implements IConfigurationManager {

    public ConfigurationManager() {
        super("configuration", Configuration.class);
        CloudAPI.getInstance().getEventManager()
            .registerWithoutBlockWarning(RedisConnectedEvent.class, event -> {
                CloudAPI.getInstance().getPacketManager().registerPacket(ConfigurationsReloadPacket.class, PacketChannel.GLOBAL);
                reloadFromDatabase();
            });
    }

    @Override
    public <T extends IConfiguration> T getConfiguration(String identifier, Class<T> clazz) {
        return (T) this.get(identifier);
    }

    @Override
    public <T extends IConfiguration> T createConfiguration(T configuration) {
        return (T) this.createBucket(configuration.getIdentifier(), (IConfiguration) configuration);
    }

    @Override
    public boolean existsConfiguration(String identifier) {
        return this.existsBucket(identifier);
    }

    @Override
    public <T extends IConfiguration> boolean deleteConfiguration(T configuration) {
        return this.existsBucket(configuration.getIdentifier());
    }

    @Override
    public boolean deleteConfiguration(String identifier) {
        return this.deleteBucket(identifier);
    }

    @Override
    public void reloadFromDatabase() {
        boolean firstLoad = this.getCachedBucketObjects().isEmpty();
        this.getCachedBucketObjects().clear();
        for (IConfiguration bucketHolder : getBucketHolders()) {
            if(!firstLoad){
                CloudAPI.getInstance().getEventManager().postLocal(new ConfigurationReloadEvent(bucketHolder));
            }
            this.getCachedBucketObjects().put(bucketHolder.getIdentifier(), (Configuration) bucketHolder);
        }
    }
}
