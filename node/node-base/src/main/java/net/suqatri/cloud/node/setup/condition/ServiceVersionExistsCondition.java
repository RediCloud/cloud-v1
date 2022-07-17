package net.suqatri.cloud.node.setup.condition;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.commons.function.BiSupplier;

public class ServiceVersionExistsCondition implements BiSupplier<String, Boolean> {
    @Override
    public Boolean supply(String s) {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .anyMatch(svh -> svh.get().getName().equalsIgnoreCase(s));
    }
}
