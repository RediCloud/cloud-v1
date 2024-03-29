package dev.redicloud.api.impl.configuration.impl;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.configuration.Configuration;
import lombok.Data;

@Data
public class LimboFallbackConfiguration extends Configuration {

    private String identifier = "limbo-fallbacks";
    private boolean enabled = true;

    @Override
    public void merged() {
        CloudAPI.getInstance().getGroupManager().existsGroupAsync("Fallback")
            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting fallback group"))
            .onSuccess(exists -> {
                if(!exists) return;
                CloudAPI.getInstance().getGroupManager().getGroupAsync("Fallback")
                    .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting fallback group"))
                    .onSuccess(group -> {
                       if(this.enabled){
                           group.setMinServices(1);
                       }else{
                           group.setMinServices(0);
                       }
                       group.updateAsync();
                    });
            });
    }
}
