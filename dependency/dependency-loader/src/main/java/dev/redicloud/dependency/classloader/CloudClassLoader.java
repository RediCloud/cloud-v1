package dev.redicloud.dependency.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public class CloudClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final URLClassPath ucp;

    public CloudClassLoader(final URL[] urls, final ClassLoader classLoader) {
        super(urls, classLoader);
        this.ucp = new URLClassPath(this);
    }

    public CloudClassLoader(ClassLoader classLoader) {
        super(new URL[0], classLoader);
        this.ucp = new URLClassPath(this);
    }

    public CloudClassLoader(final URL[] urls) {
        super(urls);
        this.ucp = new URLClassPath(this);
    }

    public void addURL(final URL url) {
        this.ucp.addUrl(url);
    }

}
