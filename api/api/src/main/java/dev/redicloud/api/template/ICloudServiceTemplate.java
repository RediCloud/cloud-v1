package dev.redicloud.api.template;

import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.api.node.ICloudNode;

import java.io.File;

public interface ICloudServiceTemplate extends IRBucketObject {

    String getName();

    File getTemplateFolder();

    String getTemplatePath(ICloudNode node);

}
