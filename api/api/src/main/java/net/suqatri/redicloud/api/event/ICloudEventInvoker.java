package net.suqatri.redicloud.api.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface ICloudEventInvoker {

    Object getListener();

    Method getMethod();

    default void invoke(Object event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        getMethod().invoke(getListener(), event);
    }

}
