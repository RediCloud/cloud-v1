package dev.redicloud.plugin.minecraft;

import dev.redicloud.api.utils.Files;
import dev.redicloud.dependency.DependencyLoader;
import dev.redicloud.module.ModuleHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftCloudPlugin extends JavaPlugin {

    private MinecraftCloudAPI cloudAPI;
    private DependencyLoader dependencyLoader;

    @Override
    public void onLoad() {
        dependencyLoader = new DependencyLoader(Files.LIBS_FOLDER.getFile(),
                Files.LIBS_REPO_FOLDER.getFile(),
                Files.LIBS_INFO_FOLDER.getFile(),
                Files.LIBS_BLACKLIST_FOLDER.getFile());
    }

    @Override
    public void onEnable() {
        this.cloudAPI = new MinecraftCloudAPI(dependencyLoader, this);
        ModuleHandler.setParentClassLoader(this.getClass().getClassLoader());
        this.cloudAPI.getModuleHandler().loadModules();
        this.cloudAPI.getModuleHandler().enableModules();
    }

    @Override
    public void onDisable() {
        if(this.cloudAPI == null) return;
        this.cloudAPI.shutdown(true);
    }
}
