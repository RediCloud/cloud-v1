package net.suqatri.cloud.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface CloudListener {
    byte priority() default EventPriority.NORMAL;
}
