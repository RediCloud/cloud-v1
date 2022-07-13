package net.suqatri.cloud.api.template;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;


public interface ICloudServiceTemplateManager {

    IRBucketHolder<ICloudServiceTemplate> getTemplate(String name);
    FutureAction<IRBucketHolder<ICloudServiceTemplate>> getTemplateAsync(String name);

    boolean existsTemplate(String name);
    FutureAction<Boolean> existsTemplateAsync(String name);

    Collection<IRBucketHolder<ICloudServiceTemplate>> getAllTemplates();
    FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getAllTemplatesAsync();
}
