package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;
import dev.redicloud.commons.WebUniqueIdFetcher;

import java.util.UUID;
import java.util.regex.Pattern;

public class PreLoginListener  {

    private final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent event){
        String name = event.getUsername();

        if(name.length() > 16){
            event.setResult(PreLoginEvent.PreLoginComponentResult
                    .denied(LegacyMessageUtils.component("Your name is too long!")));
            return;
        }

        if(name.length() < 3){
            event.setResult(PreLoginEvent.PreLoginComponentResult
                    .denied(LegacyMessageUtils.component("Your name is too short!")));
            return;
        }

        if(!pattern.matcher(name).matches()){
            event.setResult(PreLoginEvent.PreLoginComponentResult
                    .denied(LegacyMessageUtils.component("Your name is invalid!")));
            return;
        }

        CloudAPI.getInstance().getConsole().debug("Checking if player premium name " + name + " exists...");

        UUID premiumUniqueId = WebUniqueIdFetcher.fetchUniqueId(name).getBlockOrNull();
        if(premiumUniqueId == null){
            CloudAPI.getInstance().getConsole().debug("Name " + name + " is not premium!");
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
        }else{
            CloudAPI.getInstance().getConsole().debug("Name " + name + " is a premium name!");
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
        }
    }

}
