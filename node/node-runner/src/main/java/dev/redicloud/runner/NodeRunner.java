package dev.redicloud.runner;

import dev.redicloud.dependency.DependencyLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class NodeRunner {

    public static void main(String[] args) {
        DependencyLoader dependencyLoader = new DependencyLoader(new File("storage/libs")
                        , new File("storage/libs/repo")
                        , new File("storage/libs/info"), new File("storage/libs/blacklist"));

        dependencyLoader.loadDependencies();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = new URLClassLoader(dependencyLoader.getInstalledDependencies().parallelStream().map(advancedDependency -> {
            try {
                return advancedDependency.getDownloadedFile().getParentFile().toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }).toArray(URL[]::new), classLoader);

        Thread.currentThread().setContextClassLoader(urlClassLoader);
        try {
            Class<?> clazz = urlClassLoader.loadClass("dev.redicloud.node.NodeLauncherMain");
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
