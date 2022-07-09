package net.suqatri.cloud.api.impl.event;

import com.google.common.collect.ImmutableSet;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.*;
import net.suqatri.cloud.api.impl.event.packet.GlobalEventPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CloudEventHandler implements ICloudEventManager {

    private final Map<Class<?>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority = new HashMap<>();
    private final Map<Class<?>, CloudEventInvoker[]> byEventBaked = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    @Override
    public void postLocal(CloudEvent event) {
        CloudEventInvoker[] handlers = this.byEventBaked.get(event.getClass());

        if (handlers != null) {
            for (CloudEventInvoker method : handlers) {
                long start = System.nanoTime();

                try {
                    method.invoke( event );
                } catch ( IllegalAccessException ex ) {
                    throw new Error( "Method became inaccessible: " + event, ex );
                } catch ( IllegalArgumentException ex ) {
                    throw new Error( "Method rejected target/argument: " + event, ex );
                } catch ( InvocationTargetException ex ) {
                    CloudAPI.getInstance().getConsole().warn("Error dispatching event " + event + " to listener " + method.getListener(), ex.getCause());
                }

                long elapsed = System.nanoTime() - start;
                if (elapsed > 50000000) {
                    CloudAPI.getInstance().getConsole().warn("Cloud listener " + method.getListener().getClass().getName() + " took " + (elapsed / 1000000) + " to process event " + event.getClass().getName());
                }
            }
        }
    }

    @Override
    public void postGlobal(CloudGlobalEvent event) {
        postLocal(event);
        GlobalEventPacket packet = new GlobalEventPacket();
        packet.setEvent(event);
        packet.publishAsync();
    }

    private Map<Class<?>, Map<Byte, Set<Method>>> findHandlers(Object listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = new HashMap<>();
        Set<Method> methods = ImmutableSet.<Method>builder().add( listener.getClass().getMethods() ).add( listener.getClass().getDeclaredMethods() ).build();
        for (final Method m : methods) {
            CloudListener annotation = m.getAnnotation(CloudListener.class);
            if (annotation != null) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    CloudAPI.getInstance().getConsole().info("Method " + m.getName() + " in " + listener.getClass().getName() + " is annotated with @CloudListener but has " + params.length + " parameters. Only one parameter is supported.");
                    continue;
                }
                Map<Byte, Set<Method>> prioritiesMap = handler.computeIfAbsent( params[0], k -> new HashMap<>() );
                Set<Method> priority = prioritiesMap.computeIfAbsent( annotation.priority(), k -> new HashSet<>() );
                priority.add( m );
            }
        }
        return handler;
    }

    @Override
    public void register(Object listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
        this.lock.lock();
        try {
            for(Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet()) {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = this.byListenerAndPriority.computeIfAbsent( e.getKey(), k -> new HashMap<>());
                for (Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet()) {
                    Map<Object, Method[]> currentPriorityMap = prioritiesMap.computeIfAbsent( entry.getKey(), k -> new HashMap<>());
                    currentPriorityMap.put(listener, entry.getValue().toArray( new Method[0]));
                }
                bakeHandlers(e.getKey());
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void unregister(Object listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers(listener);
        this.lock.lock();
        try {
            for(Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet()) {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = this.byListenerAndPriority.get(e.getKey());
                if (prioritiesMap != null) {
                    for (Byte priority : e.getValue().keySet()) {
                        Map<Object, Method[]> currentPriority = prioritiesMap.get(priority);
                        if (currentPriority != null) {
                            currentPriority.remove(listener);
                            if (currentPriority.isEmpty()) {
                                prioritiesMap.remove(priority);
                            }
                        }
                    }
                    if (prioritiesMap.isEmpty()) {
                        this.byListenerAndPriority.remove(e.getKey());
                    }
                }
                bakeHandlers(e.getKey());
            }
        } finally {
            this.lock.unlock();
        }
    }

    private void bakeHandlers(Class<?> eventClass) {
        Map<Byte, Map<Object, Method[]>> handlersByPriority = this.byListenerAndPriority.get(eventClass);
        if(handlersByPriority != null) {
            List<ICloudEventInvoker> handlersList = new ArrayList<>(handlersByPriority.size() * 2);

            byte value = Byte.MIN_VALUE;
            do {
                Map<Object, Method[]> handlersByListener = handlersByPriority.get(value);
                if (handlersByListener != null) {
                    for (Map.Entry<Object, Method[]> listenerHandlers : handlersByListener.entrySet()) {
                        for (Method method : listenerHandlers.getValue()) {
                            ICloudEventInvoker ehm = new CloudEventInvoker(listenerHandlers.getKey(), method);
                            handlersList.add( ehm );
                        }
                    }
                }
            } while (value++ < Byte.MAX_VALUE);
            this.byEventBaked.put(eventClass, handlersList.toArray(new CloudEventInvoker[0]));
        } else {
            this.byEventBaked.remove(eventClass);
        }
    }
}
