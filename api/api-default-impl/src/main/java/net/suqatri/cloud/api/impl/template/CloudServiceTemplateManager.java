package net.suqatri.cloud.api.impl.template;

import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.File;
import java.util.Collection;

public class CloudServiceTemplateManager extends RedissonBucketManager<CloudServiceTemplate, ICloudServiceTemplate> implements ICloudServiceTemplateManager {

    public CloudServiceTemplateManager() {
        super("template", ICloudServiceTemplate.class);
    }

    @Override
    public IRBucketHolder<ICloudServiceTemplate> getTemplate(String name) {
        return this.getBucketHolder(name);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> getTemplateAsync(String name) {
        return this.getBucketHolderAsync(name);
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
    public Collection<IRBucketHolder<ICloudServiceTemplate>> getAllTemplates() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getAllTemplatesAsync() {
        return this.getBucketHoldersAsync();
    }
}
