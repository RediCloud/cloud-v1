package net.suqatri.redicloud.api.template;

import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;

import java.io.File;

public interface ICloudServiceTemplate extends IRBucketObject {

    String getName();

    File getTemplateFolder();

    String getTemplatePath(ICloudNode node);

}
