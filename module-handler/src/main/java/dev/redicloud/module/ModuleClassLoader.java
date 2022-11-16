package dev.redicloud.module;

import lombok.Getter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ModuleClassLoader extends URLClassLoader {

    @Getter
    private final ModuleDescription description;

    public ModuleClassLoader(ModuleDescription description) throws MalformedURLException {
        super(new URL[] {description.getFile().toURI().toURL()} );
        this.description = description;
    }

    public CloudModule loadClass() throws Exception{
        Class<?> bootstrap = loadClass(description.getMainClasse());
        CloudModule cloudModule = (CloudModule) bootstrap.newInstance();

        cloudModule.setClassLoader(this);
        cloudModule.setDescription(this.description);

        cloudModule.onLoad();

        return cloudModule;
    }
}
