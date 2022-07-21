package net.suqatri.redicloud.node.setup.condition;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.commons.function.BiSupplier;

public class ServiceVersionExistsCondition implements BiSupplier<String, Boolean> {
    @Override
    public Boolean supply(String s) {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .noneMatch(svh -> svh.get().getName().equalsIgnoreCase(s));
    }
}
