package net.suqatri.cloud.api.template;

import java.io.File;

public interface ICloudServiceTemplate {

    String getName();
    File getTemplateFolder();
    void updateToNodes();

}
