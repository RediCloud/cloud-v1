package net.suqatri.cloud.api.template;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketObject;

import java.io.File;

public interface ICloudServiceTemplate extends IRBucketObject {

    String getName();
    File getTemplateFolder();
    String getTemplatePath(ICloudNode node);

}
