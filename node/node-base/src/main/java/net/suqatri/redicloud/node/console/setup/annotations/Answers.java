package net.suqatri.redicloud.node.console.setup.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface Answers {

    String[] forbidden() default {};

    String[] only() default {};

}
