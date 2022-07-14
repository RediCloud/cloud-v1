package net.suqatri.cloud.node.console.setup.conditions;

import net.suqatri.cloud.commons.function.BiSupplier;

public class PositivIntegerCondition implements BiSupplier<String, Boolean> {

    @Override
    public Boolean supply(String s) {
        try {
            int i = Integer.parseInt(s);
            return i < -1;
        }catch (Exception e){
            return false;
        }
    }
}
