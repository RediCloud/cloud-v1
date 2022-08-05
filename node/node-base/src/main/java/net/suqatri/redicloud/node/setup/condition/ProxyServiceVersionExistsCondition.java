package net.suqatri.redicloud.node.setup.condition;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.commons.function.BiSupplier;

public class ProxyServiceVersionExistsCondition implements BiSupplier<String, Boolean> {
    @Override
    public Boolean supply(String s) {
        System.out.println("Size: " + CloudAPI.getInstance().getServiceVersionManager().getServiceVersions().size());
        System.out.println("TypeSize: " + CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .filter(holder -> holder.get().getEnvironmentType() == ServiceEnvironment.BUNGEECORD
                        || holder.get().getEnvironmentType() == ServiceEnvironment.VELOCITY).count());
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .filter(holder -> holder.get().getEnvironmentType() == ServiceEnvironment.BUNGEECORD
                        || holder.get().getEnvironmentType() == ServiceEnvironment.VELOCITY)
                .noneMatch(svh -> {
                    System.out.println("Check: " + svh.get().getName() + " | " + s);
                    return svh.get().getName().equalsIgnoreCase(s);
                });
    }
}
