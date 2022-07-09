package net.suqatri.cloud.node.console.setup.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SuggestedAnswer {

    /**
     * The suggested answer that will be pre-filled in console
     */
    String value();
}
