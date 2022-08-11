package dev.redicloud.api.impl.template;

import dev.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import dev.redicloud.api.template.ICloudServiceTemplate;
import dev.redicloud.api.template.ICloudServiceTemplateManager;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public class CloudServiceTemplateManager extends RedissonBucketManager<CloudServiceTemplate, ICloudServiceTemplate> implements ICloudServiceTemplateManager {

    public CloudServiceTemplateManager() {
        super("template", CloudServiceTemplate.class);
    }

    @Override
    public ICloudServiceTemplate getTemplate(String name) {
        return this.get(name.toLowerCase());
    }

    @Override
    public FutureAction<ICloudServiceTemplate> getTemplateAsync(String name) {
        return this.getAsync(name.toLowerCase());
    }

    @Override
    public boolean existsTemplate(String name) {
        return this.existsBucket(name.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> existsTemplateAsync(String name) {
        return this.existsBucketAsync(name.toLowerCase());
    }

    @Override
    public Collection<ICloudServiceTemplate> getAllTemplates() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<ICloudServiceTemplate>> getAllTemplatesAsync() {
        return this.getBucketHoldersAsync();
    }
}
