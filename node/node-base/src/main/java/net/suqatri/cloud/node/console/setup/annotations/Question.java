package net.suqatri.cloud.node.console.setup.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Question {

    /**
     * Represents the id of the current question
     */
    int id();

    /**
     * Represents the question of this instance
     */
    String question();

}
