package dev.redicloud.api.impl.event;

import dev.redicloud.api.event.ICloudEventInvoker;

import java.lang.reflect.Method;

public class CloudEventInvoker implements ICloudEventInvoker {

    private final Object listener;
    private final Method method;

    public CloudEventInvoker(Object listener, Method method) {
        this.listener = listener;
        this.method = method;
    }

    @Override
    public Object getListener() {
        return this.listener;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }
}
