package net.suqatri.redicloud.api.impl.template;

import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.template.ICloudServiceTemplate;
import net.suqatri.redicloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public class CloudServiceTemplateManager extends RedissonBucketManager<CloudServiceTemplate, ICloudServiceTemplate> implements ICloudServiceTemplateManager {

    public CloudServiceTemplateManager() {
        super("template", CloudServiceTemplate.class);
    }

    @Override
    public ICloudServiceTemplate getTemplate(String name) {
        return this.get(name);
    }

    @Override
    public FutureAction<ICloudServiceTemplate> getTemplateAsync(String name) {
        return this.getAsync(name);
    }

    @Override
    public boolean existsTemplate(String name) {
        return this.existsBucket(name);
    }

    @Override
    public FutureAction<Boolean> existsTemplateAsync(String name) {
        return this.existsBucketAsync(name);
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
