package net.suqatri.cloud.api.impl.service.version;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;

@Getter
@Setter
public class CloudServiceVersion extends RBucketObject implements ICloudServiceVersion {

    private String name;
    private String downloadUrl;
    private ServiceEnvironment environmentType;
    private boolean isPaperClip;

}
