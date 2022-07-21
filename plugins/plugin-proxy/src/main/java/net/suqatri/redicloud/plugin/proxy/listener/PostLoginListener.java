package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.plugin.proxy.ProxyCloudAPI;

public class PostLoginListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        ProxyCloudAPI.getInstance().getService().setOnlineCount(ProxyServer.getInstance().getOnlineCount());
        ProxyCloudAPI.getInstance().getService().updateAsync();
    }

}
