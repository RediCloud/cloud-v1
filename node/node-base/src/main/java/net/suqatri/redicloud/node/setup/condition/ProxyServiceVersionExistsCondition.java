package net.suqatri.redicloud.node.setup.condition;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.commons.function.BiSupplier;

public class ProxyServiceVersionExistsCondition implements BiSupplier<String, Boolean> {
    @Override
    public Boolean supply(String s) {
        s = s.replaceAll(" ", "");
        for (IRBucketHolder<ICloudServiceVersion> holder : CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()) {
            if(holder.get().getEnvironmentType() != ServiceEnvironment.BUNGEECORD
                    && holder.get().getEnvironmentType() != ServiceEnvironment.VELOCITY) continue;
            if(holder.get().getName().equalsIgnoreCase(s)) return false;
        }
        return true;
    }
}
