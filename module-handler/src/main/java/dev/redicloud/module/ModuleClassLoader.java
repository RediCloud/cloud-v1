package dev.redicloud.module;

import com.google.common.io.ByteStreams;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ModuleClassLoader extends URLClassLoader {

    public static final List<ModuleClassLoader> loaders = new ArrayList<>();

    @Getter
    private final ModuleDescription description;
    private final JarFile jarFile;
    private final Manifest manifest;
    private final URL url;

    public ModuleClassLoader(ModuleDescription description, ClassLoader parent) throws IOException {
        super(new URL[] {description.getFile().toURI().toURL()}, parent);
        this.description = description;
        this.jarFile = new JarFile(description.getFile());
        this.manifest = this.jarFile.getManifest();
        this.url = description.getFile().toURI().toURL();

        loaders.add(this);
    }

    public CloudModule loadClass() throws Exception{
        Class<?> bootstrap = loadClass(description.getMainClass());
        CloudModule cloudModule = (CloudModule) bootstrap.newInstance();

        cloudModule.setClassLoader(this);
        cloudModule.setDescription(this.description);

        cloudModule.onLoad();

        return cloudModule;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return this.loadClass0(name, resolve);
    }

    public Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException {

        try {
            Class<?> result = super.loadClass(name, resolve);
            if (result != null) {
                return result;
            }
        } catch (ClassNotFoundException ignore) {}

        for (ModuleClassLoader loader : loaders) {
            if(loader == this) continue;
            return loader.loadClass0(name, resolve);
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        JarEntry entry = this.jarFile.getJarEntry(path);

        if (entry != null) {
            byte[] classBytes;

            try (InputStream is = this.jarFile.getInputStream(entry)) {
                classBytes = ByteStreams.toByteArray(is);
            } catch (IOException ex) {
                throw new ClassNotFoundException(name, ex);
            }

            int dot = name.lastIndexOf('.');
            if (dot != -1) {
                String pkgName = name.substring(0, dot);
                if (getPackage(pkgName) == null) {
                    try {
                        if (this.manifest != null) {
                            definePackage(pkgName, manifest, url);
                        } else {
                            definePackage(pkgName, null, null, null, null, null, null, null);
                        }
                    } catch (IllegalArgumentException ex) {
                        if (getPackage(pkgName) == null) {
                            throw new IllegalStateException("Cannot find package " + pkgName);
                        }
                    }
                }
            }

            CodeSigner[] signers = entry.getCodeSigners();
            CodeSource source = new CodeSource(url, signers);

            return defineClass(name, classBytes, 0, classBytes.length, source);
        }

        return super.findClass(name);
    }

}
