package dev.redicloud.node.console.setup.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExitAfterInput {

    /**
     * The values that cause an exit if they are provided
     */
    String[] value();
}
