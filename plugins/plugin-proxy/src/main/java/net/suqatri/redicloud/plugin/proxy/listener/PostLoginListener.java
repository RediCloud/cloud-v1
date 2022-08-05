package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.plugin.proxy.ProxyCloudAPI;

public class PostLoginListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        if(ProxyCloudAPI.getInstance().getService().isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")){
            event.getPlayer().disconnect("§cThis proxy is currently under maintenance. Please try again later.");
            return;
        }
    }

}
