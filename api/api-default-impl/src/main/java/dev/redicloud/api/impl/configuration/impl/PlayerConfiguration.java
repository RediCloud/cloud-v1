package dev.redicloud.api.impl.configuration.impl;

import dev.redicloud.api.impl.configuration.Configuration;
import lombok.Data;
import dev.redicloud.api.CloudAPI;

@Data
public class PlayerConfiguration extends Configuration {

    private String identifier = "players";
    private boolean allowCracked = false;
    private int minPasswordLength = 6;
    private int maxPasswordLength = 18;
    private boolean passwordCanContainsPlayerName = false;

    @Override
    public void merged() {
        CloudAPI.getInstance().getGroupManager().existsGroupAsync("Verify")
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting verify group"))
                .onSuccess(exists -> {
                    if(!exists) return;
                    CloudAPI.getInstance().getGroupManager().getGroupAsync("Verify")
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting verify group"))
                            .onSuccess(group -> {
                                if(this.allowCracked){
                                    group.setMinServices(1);
                                }else{
                                    group.setMinServices(0);
                                }
                                group.updateAsync();
                            });
                });
    }
}
