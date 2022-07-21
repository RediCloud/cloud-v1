package net.suqatri.redicloud.node.console.setup.conditions;

import net.suqatri.redicloud.commons.function.BiSupplier;

public class PositivIntegerCondition implements BiSupplier<String, Boolean> {

    @Override
    public Boolean supply(String s) {
        try {
            int i = Integer.parseInt(s);
            return i < 0;
        }catch (Exception e){
            return false;
        }
    }
}
