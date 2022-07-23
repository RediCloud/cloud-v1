package net.suqatri.redicloud.runner.dependency.classloader;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemClassloaderUtil {

    private static boolean REMIND = false;

    public static void setSystemClassloader(final ClassLoader classloader) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field scl = null;
        Label_0149:
        {
            try {
                scl = ClassLoader.class.getDeclaredField("scl");
            } catch (NoSuchFieldException e) {
                try {
                    final Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", Boolean.TYPE);
                    getDeclaredFields0.setAccessible(true);
                    final Field[] array;
                    final Field[] fields = array = (Field[]) getDeclaredFields0.invoke(ClassLoader.class, false);
                    for (final Field classField : array) {
                        if ("scl".equals(classField.getName())) {
                            classField.setAccessible(true);
                            scl = classField;
                            break;
                        }
                    }
                } catch (Throwable throwable) {
                    if (SystemClassloaderUtil.REMIND) {
                        break Label_0149;
                    }
                    SystemClassloaderUtil.REMIND = true;
                    System.err.println("Unable to override class loader. please add --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED to your startup flag.");
                    System.err.println("This is fine to ignore if you didn't face any problems.");
                }
            }
        }
        if (scl == null) {
            return;
        }
        Unsafe unsafe;
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            unsafe.ensureClassInitialized(MethodHandles.Lookup.class);
        } catch (Throwable t) {
            throw new IllegalStateException("Unsafe not found");
        }
        final long sclOffset = unsafe.staticFieldOffset(scl);
        unsafe.putObjectVolatile(ClassLoader.class, sclOffset, classloader);
    }


}
