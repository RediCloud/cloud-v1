package dev.redicloud.api.impl.template;

import dev.redicloud.api.impl.redis.bucket.RBucketObject;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.template.ICloudServiceTemplate;
import dev.redicloud.api.utils.Files;

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

    @Override
    public String getIdentifier() {
        return this.name;
    }
}
