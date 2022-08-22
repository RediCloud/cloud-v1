package dev.redicloud.plugin.minecraft;

import dev.redicloud.api.utils.Files;
import dev.redicloud.dependency.DependencyLoader;
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
        this.cloudAPI.getModuleHandler().loadModules();
    }

    @Override
    public void onDisable() {
        this.cloudAPI.shutdown(true);
    }
}
