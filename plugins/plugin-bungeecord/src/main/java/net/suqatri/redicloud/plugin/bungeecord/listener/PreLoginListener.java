package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.commons.WebUniqueIdFetcher;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.util.regex.Pattern;

public class PreLoginListener implements Listener {

    private final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event){
        PendingConnection connection = event.getConnection();
        InitialHandler initialHandler = (InitialHandler)connection;
        String name = connection.getName();

        event.registerIntent(BungeeCordCloudAPI.getInstance().getPlugin());

        if(name.length() > 16){
            event.setCancelled(true);
            event.setCancelReason("Your name is too long!");
            return;
        }

        if(name.length() < 3){
            event.setCancelled(true);
            event.setCancelReason("Your name is too short!");
            return;
        }

        if(!pattern.matcher(name).matches()){
            event.setCancelled(true);
            event.setCancelReason("Your name is invalid!");
            return;
        }

        CloudAPI.getInstance().getConsole().debug("Checking if player premium name " + name + " exists...");

        WebUniqueIdFetcher.fetchUniqueId(name)
            .onFailure(throwable -> {
                CloudAPI.getInstance().getConsole().debug("Name " + name + " is not premium!");
                event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
            }).onSuccess(uniqueId -> {
                CloudAPI.getInstance().getConsole().debug("Name " + name + " is a premium name!");
                connection.setOnlineMode(true);
                event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
            });
    }

}
