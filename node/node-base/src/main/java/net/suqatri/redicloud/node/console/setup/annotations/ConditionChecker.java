package net.suqatri.redicloud.node.console.setup.annotations;

import net.suqatri.redicloud.commons.function.BiSupplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConditionChecker {

    Class<? extends BiSupplier<String, Boolean>> value();

    String message();
}
