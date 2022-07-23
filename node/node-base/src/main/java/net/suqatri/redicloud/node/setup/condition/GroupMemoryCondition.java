package net.suqatri.redicloud.node.setup.condition;

import net.suqatri.redicloud.commons.ConditionChecks;
import net.suqatri.redicloud.commons.function.BiSupplier;

public class GroupMemoryCondition implements BiSupplier<String, Boolean> {

    @Override
    public Boolean supply(String s) {
        if (!ConditionChecks.isInteger(s)) return false;
        return Integer.parseInt(s) < 500;
    }
}
