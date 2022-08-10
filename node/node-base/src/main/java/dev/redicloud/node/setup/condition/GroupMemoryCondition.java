package dev.redicloud.node.setup.condition;

import dev.redicloud.commons.ConditionChecks;
import dev.redicloud.commons.function.BiSupplier;

public class GroupMemoryCondition implements BiSupplier<String, Boolean> {

    @Override
    public Boolean supply(String s) {
        if (!ConditionChecks.isInteger(s)) return false;
        return Integer.parseInt(s) < 500;
    }
}
