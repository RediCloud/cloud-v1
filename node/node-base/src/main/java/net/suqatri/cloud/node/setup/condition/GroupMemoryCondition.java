package net.suqatri.cloud.node.setup.condition;

import net.suqatri.cloud.commons.ConditionChecks;
import net.suqatri.cloud.commons.function.BiSupplier;
import net.suqatri.cloud.node.console.setup.annotations.ConditionChecker;

import java.lang.annotation.Annotation;

public class GroupMemoryCondition implements BiSupplier<String, Boolean> {

    @Override
    public Boolean supply(String s) {
        if(!ConditionChecks.isInteger(s)) return false;
        return Integer.parseInt(s) < 500;
    }
}
