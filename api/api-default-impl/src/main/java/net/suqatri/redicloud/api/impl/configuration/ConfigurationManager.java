package net.suqatri.redicloud.api.impl.configuration;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.configuration.IConfiguration;
import net.suqatri.redicloud.api.configuration.IConfigurationManager;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;

public class ConfigurationManager extends RedissonBucketManager<Configuration, IConfiguration> implements IConfigurationManager {

    public ConfigurationManager() {
        super("configuration", Configuration.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> reloadFromDatabase());
    }

    @Override
    public <T extends IConfiguration> T getConfiguration(String identifier, Class<T> clazz) {
        return (T) this.get(identifier);
    }

    @Override
    public <T extends IConfiguration> T create(T configuration) {
        return (T) this.createBucket(configuration.getIdentifier(), (IConfiguration) configuration);
    }

    @Override
    public boolean exists(String identifier) {
        return this.existsBucket(identifier);
    }

    @Override
    public <T extends IConfiguration> boolean delete(T configuration) {
        return this.existsBucket(configuration.getIdentifier());
    }

    @Override
    public boolean delete(String identifier) {
        return this.deleteBucket(identifier);
    }

    @Override
    public void reloadFromDatabase() {
        this.getCachedBucketObjects().clear();
        for (IConfiguration bucketHolder : getBucketHolders()) {
            this.getCachedBucketObjects().put(bucketHolder.getIdentifier(), (Configuration) bucketHolder);
        }
    }
}
