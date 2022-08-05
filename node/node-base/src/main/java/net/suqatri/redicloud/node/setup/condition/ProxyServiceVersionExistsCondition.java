package net.suqatri.redicloud.node.setup.condition;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.commons.function.BiSupplier;

public class ProxyServiceVersionExistsCondition implements BiSupplier<String, Boolean> {
    @Override
    public Boolean supply(String s) {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .filter(holder -> holder.get().getEnvironmentType() == ServiceEnvironment.BUNGEECORD
                        || holder.get().getEnvironmentType() == ServiceEnvironment.VELOCITY)
                .noneMatch(svh -> svh.get().getName().equalsIgnoreCase(s));
    }
}
