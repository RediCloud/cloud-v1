package dev.redicloud.runner.dependency.classloader;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

public class URLClassPath {

    private final URLClassLoader classLoader;

    public URLClassPath(final URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addUrl(final URL url) {
        Unsafe unsafe;
        MethodHandles.Lookup lookup;
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            unsafe.ensureClassInitialized(MethodHandles.Lookup.class);
            final Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            final Object lookupBase = unsafe.staticFieldBase(lookupField);
            final long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable t) {
            throw new IllegalStateException("Unsafe not found");
        }
        Field field;
        try {
            field = URLClassLoader.class.getDeclaredField("ucp");
        } catch (NoSuchFieldException e2) {
            throw new RuntimeException("Couldn't find ucp field from ClassLoader!");
        }
        try {
            final long ucpOffset = unsafe.objectFieldOffset(field);
            final Object ucp = unsafe.getObject(this.classLoader, ucpOffset);
            final MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(Void.TYPE, URL.class));
            methodHandle.invoke(ucp, url);
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while adding spigot.jar to class path!", throwable);
        }
    }
}
