package net.suqatri.cloud.api.template;

import java.io.File;

public interface ICloudServiceTemplateManager {

    ICloudServiceTemplate getTemplate(String name);
    boolean existsTemplate(String name);
    void createTemplate(String name, File folder);

}
