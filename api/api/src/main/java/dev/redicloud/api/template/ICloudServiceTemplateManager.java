package dev.redicloud.api.template;

import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;


public interface ICloudServiceTemplateManager {

    ICloudServiceTemplate getTemplate(String name);

    FutureAction<ICloudServiceTemplate> getTemplateAsync(String name);

    boolean existsTemplate(String name);

    FutureAction<Boolean> existsTemplateAsync(String name);

    Collection<ICloudServiceTemplate> getAllTemplates();

    FutureAction<Collection<ICloudServiceTemplate>> getAllTemplatesAsync();


}
