package dev.redicloud.runner.dependency.classloader;

public class ClassLoaderDebug {

    public ClassLoaderDebug() {
        System.out.println("System Class Loader:");
        System.out.println("- " + ClassLoader.getSystemClassLoader().getClass().getName());
        System.out.println("Current Class Loader:");
        System.out.println("- " + this.getClass().getClassLoader().getClass().getName());
        System.out.println("Thread Context Class Loader:");
        System.out.println("- " + Thread.currentThread().getContextClassLoader().getClass().getName());
    }

}
