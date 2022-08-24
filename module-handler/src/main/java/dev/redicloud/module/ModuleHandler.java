package dev.redicloud.module;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.console.LogLevel;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.api.utils.Files;
import dev.redicloud.dependency.DependencyLoader;
import org.redisson.codec.JsonJacksonCodec;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleHandler {

    private final LinkedHashMap<ModuleDescription, CloudModule> enabledModules;
    private final LinkedHashMap<ModuleDescription, CloudModule> onlyLoadedModules;
    private final List<ModuleDescription> toLoad;
    private final List<String> errorWhileLoading;
    private final File moduleFolder;
    private final ApplicationType applicationType;
    private final DependencyLoader dependencyLoader;

    public ModuleHandler(DependencyLoader loader, ApplicationType applicationType) {
        this.moduleFolder = Files.MODULES_FOLDER.getFile();
        this.dependencyLoader = loader;
        this.applicationType = applicationType;
        if(!this.moduleFolder.exists()) this.moduleFolder.mkdirs();
        this.enabledModules = new LinkedHashMap<>();
        this.onlyLoadedModules = new LinkedHashMap<>();
        this.errorWhileLoading = new ArrayList<>();
        this.toLoad = new ArrayList<>();
    }

    public void loadModules(){
        detectModules();
        if(this.toLoad.isEmpty()) {
            if(CloudAPI.getInstance().getConsole().canLog(LogLevel.DEBUG)){
                CloudAPI.getInstance().getConsole().info("Detected no module to load in folder " + moduleFolder.getAbsolutePath());
            }else{
                CloudAPI.getInstance().getConsole().info("Detected no module to load!");
            }
            return;
        }else{
            CloudAPI.getInstance().getConsole().info("Detected " + this.toLoad.size() + " to load!");
        }
        this.toLoad.forEach(description -> {
            CloudAPI.getInstance().getConsole().info("Loading module " + description.getName());
            loadModule(description, new ArrayList<>());
        });
        this.toLoad.clear();
    }

    public void enableModules(){
        this.onlyLoadedModules.values().forEach(module -> {
            CloudAPI.getInstance().getConsole().info("Enabling module " + module.getDescription().getName());
            module.onEnable();
        });
        this.onlyLoadedModules.clear();
    }

    public void unloadModules(){
        this.enabledModules.values().forEach(CloudModule::onDisable);
        this.enabledModules.clear();
        this.onlyLoadedModules.clear();
    }

    public boolean isLoaded(ModuleDescription moduleDescription){
        if(this.enabledModules.keySet().parallelStream().anyMatch(d -> d.getName().equals(moduleDescription.getName()))) return true;
        return this.onlyLoadedModules.keySet().parallelStream().anyMatch(d -> d.getName().equals(moduleDescription.getName()));
    }

    public boolean isEnabled(ModuleDescription moduleDescription){
        return this.enabledModules.keySet().parallelStream().anyMatch(d -> d.getName().equals(moduleDescription.getName()));
    }

    public boolean isBlockedLoading(String name){
        return this.errorWhileLoading.contains(name);
    }

    public void blockLoading(String name){
        this.errorWhileLoading.add(name);
    }

    public void blockLoading(ModuleDescription description){
        this.errorWhileLoading.add(description.getName());
    }

    public boolean loadModule(ModuleDescription description, List<String> calledModules){

        if(isBlockedLoading(description.getName())) return true;

        if(isLoaded(description)) return false;

        List<String> dependModules = new ArrayList<>();
        if(description.getDependModules() != null) dependModules.addAll(description.getDependModules());
        if(description.getSoftDependModules() != null) dependModules.addAll(description.getSoftDependModules());
        for (String dependModuleName : dependModules) {
            if(isLoaded(ModuleDescription.ofName(dependModuleName))) continue;
            if(this.toLoad.parallelStream().noneMatch(module -> module.getName().equals(dependModuleName))){
                if(description.getSoftDependModules().contains(dependModuleName)) {
                    CloudAPI.getInstance().getConsole()
                            .warn("Warn while load module " + description.getName() + " because depend module " + dependModuleName + " is unavailable but its only soft depend!");
                }else {
                    CloudAPI.getInstance().getConsole()
                            .error("Can´t load module " + description.getName() + " because depend module " + dependModuleName + " is unavailable!");
                    blockLoading(description.getName());
                    return true;
                }
            }
            for (ModuleDescription dependModuleDescription : this.toLoad) {
                if(dependModuleDescription.getName().equals(dependModuleName)){
                    if(calledModules.contains(dependModuleName) && !description.getSoftDependModules().contains(dependModuleName)){
                        CloudAPI.getInstance().getConsole()
                                .error("Circular dependency detected for module " + description.getName() + " (target module: " + dependModuleName + ")");
                        blockLoading(description);
                        blockLoading(dependModuleName);
                        return true;
                    }
                    calledModules.add(description.getName());
                    boolean blocked = loadModule(dependModuleDescription, calledModules);
                    if(blocked) {
                        if(dependModuleDescription.getSoftDependModules().contains(dependModuleName)) {
                            CloudAPI.getInstance().getConsole()
                                    .warn("Warn while load module " + description.getName() + " because dependencies are unavailable but its only soft depend!");
                        }else {
                            CloudAPI.getInstance().getConsole()
                                    .error("Can´t load module " + description.getName() + " because dependencies are unavailable!");
                            blockLoading(description);
                            return true;
                        }
                    }
                }
            }
        }

        if(isBlockedLoading(description.getName())) return true;

        if(description.getFile() != null) {
            try {
                this.dependencyLoader.addJarFiles(Collections.singletonList(description.getFile()));
            } catch (Exception e) {
                CloudAPI.getInstance().getConsole().error("Can't load module " + description.getName() + " because of an error while loading into class path!");
                blockLoading(description);
                return true;
            }
        }
        //TODO: Load here dependency via dependency loader...
        try {
            CloudAPI.getInstance().getConsole().info("Loading " + description.getName() + " module with " + getClass().getClassLoader().getClass().getName() + "!");
            Class<?> mainClass = Thread.currentThread().getContextClassLoader().loadClass(description.getMainClasses());
            CloudModule cloudModule = (CloudModule) mainClass.newInstance();

            Class<?> clazzToModify = mainClass;
            while(!clazzToModify.getName().equals(CloudModule.class.getName())){
                if(clazzToModify.getSuperclass() == null){
                    CloudAPI.getInstance().getConsole()
                            .error(mainClass.getName() + " is not a CloudModule!");
                    blockLoading(description);
                    return true;
                }
                clazzToModify = clazzToModify.getSuperclass();
            }

            Field descriptionField = clazzToModify.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(cloudModule, description);
            descriptionField.setAccessible(false);

            cloudModule.onLoad();
            this.onlyLoadedModules.put(description, cloudModule);
        } catch (ClassNotFoundException e) {
            CloudAPI.getInstance().getConsole().error("Can´t find main class of module " + description.getName() + " (" + description.getMainClasses() + ")", e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void disableModule(CloudModule cloudModule){
        this.onlyLoadedModules.remove(cloudModule.getDescription());
        this.enabledModules.remove(cloudModule.getDescription());
        cloudModule.onDisable();
    }

    public void detectModules() {
        JsonJacksonCodec codec = new JsonJacksonCodec();
        if(!this.moduleFolder.exists()) this.moduleFolder.mkdirs();
        for (File file : this.moduleFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith( ".jar" )) {
                try (JarFile jar = new JarFile(file)) {
                    JarEntry entry = jar.getJarEntry( "module.json" );
                    if(entry == null) continue;

                    try (InputStream in = jar.getInputStream(entry)) {
                        ModuleDescription moduleDescription = codec.getObjectMapper().readValue(in, ModuleDescription.class);
                        this.toLoad.add(moduleDescription);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
