package net.suqatri.cloud.api.template;

import java.io.File;
import java.io.Serializable;

public interface ICloudServiceTemplate extends Serializable {

    String getName();
    File getTemplateFolder();
    void updateToNodes();

}
