package net.suqatri.cloud.api.impl.template;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.commons.file.Files;

import java.io.File;

@Getter
@Setter
public class CloudServiceTemplate extends RBucketObject implements ICloudServiceTemplate {

    private String name;

    @Override
    public File getTemplateFolder() {
        return new File(Files.TEMPLATE_FOLDER.getFile(), this.name);
    }

    @Override
    public void updateToNodes() {
        //TODO File Manager
    }
}
