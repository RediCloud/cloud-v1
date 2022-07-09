package net.suqatri.cloud.runner;

import net.suqatri.cloud.runner.dependency.DependencyLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;

public class NodeRunner {

    public static void main(String[] args) {

        File nodeFile = new File("storage/node.jar");

        DependencyLoader dependencyLoader = new DependencyLoader(new File("/dependency"), new File("/dependency/repo"), new File("/dependency/info"),new File("/dependency/blacklist"));
        dependencyLoader.loadDependencies();
        List<URL> urls = dependencyLoader.getInstalledDependencies().parallelStream().map(advancedDependency -> {
            try {
                return advancedDependency.toURL();
            } catch (MalformedURLException e) {
                System.out.println("Failed to import dependency " + advancedDependency.getName());
                e.printStackTrace();
            }
            return null;
        }).filter(u -> u != null).collect(Collectors.toList());
        try {
            urls.add(nodeFile.toURI().toURL());
        } catch (MalformedURLException e) {
            System.out.println("Failed to load node.jar");
            e.printStackTrace();
        }

        ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), currentContextClassLoader);

        Thread.currentThread().setContextClassLoader(urlClassLoader);

        try {
            urlClassLoader.loadClass("net.suqatri.cloud.node.NodeLauncherMain")
                    .getMethod("main", String[].class)
                    .invoke(null, (Object) args);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.out.println("Failed to load node launcher class!");
            e.printStackTrace();
        }
    }

}
