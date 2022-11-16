package dev.redicloud.module;

import dev.redicloud.api.CloudAPI;
import lombok.Getter;

public abstract class CloudModule {

    @Getter
    private ModuleDescription description = null;
    private ModuleClassLoader classLoader = null;

    public void onLoad(){}
    public void onEnable(){}
    public void onDisable(){}

    public final void setClassLoader(ModuleClassLoader classLoader){
        this.classLoader = classLoader;
    }

    public final void setDescription(ModuleDescription description) {
        this.description = description;
    }

    public CloudAPI getApi(){
        return CloudAPI.getInstance();
    }

}
