package dev.redicloud.node.setup.condition;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.commons.function.BiSupplier;

public class ProxyServiceVersionExistsCondition implements BiSupplier<String, Boolean> {
    @Override
    public Boolean supply(String s) {
        s = s.replaceAll(" ", "");
        for (ICloudServiceVersion holder : CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()) {
            if(holder.getEnvironmentType() != ServiceEnvironment.BUNGEECORD
                    && holder.getEnvironmentType() != ServiceEnvironment.VELOCITY) continue;
            if(holder.getName().equalsIgnoreCase(s)) return false;
        }
        return true;
    }
}
