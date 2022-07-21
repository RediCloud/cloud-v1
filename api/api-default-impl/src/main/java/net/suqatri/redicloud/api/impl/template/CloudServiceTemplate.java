package net.suqatri.redicloud.api.impl.template;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.template.ICloudServiceTemplate;
import net.suqatri.redicloud.api.utils.Files;

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
    public String getTemplatePath(ICloudNode node) {
        return new File(node.getFilePath(Files.TEMPLATE_FOLDER), this.name).getAbsolutePath();
    }
}
