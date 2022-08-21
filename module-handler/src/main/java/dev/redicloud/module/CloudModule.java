package dev.redicloud.module;

import dev.redicloud.api.CloudAPI;
import lombok.Getter;

public abstract class CloudModule {

    @Getter
    private final ModuleDescription description = null;

    void onLoad(){}
    void onEnable(){}
    void onDisable(){}

    public CloudAPI getApi(){
        return CloudAPI.getInstance();
    }

}
