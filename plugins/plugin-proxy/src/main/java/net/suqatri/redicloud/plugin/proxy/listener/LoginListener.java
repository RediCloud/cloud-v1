package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.plugin.proxy.ProxyCloudAPI;

import java.util.UUID;

public class LoginListener implements Listener {

    @EventHandler
    public void onLogin(LoginEvent event) {
        UUID uniqueId = event.getConnection().getUniqueId();

        event.registerIntent(ProxyCloudAPI.getInstance().getPlugin());

        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(uniqueId)
                .onFailure(throwable -> {
                    event.setCancelled(true);
                    event.completeIntent(ProxyCloudAPI.getInstance().getPlugin());
                    event.getConnection().disconnect(throwable.getMessage());
                    CloudAPI.getInstance().getConsole().error("Error while checking if player " + event.getConnection().getUniqueId() + "exists!", throwable);
                }).onSuccess(exists -> {
                    if (!exists) {
                        CloudPlayer cloudPlayer = new CloudPlayer();
                        cloudPlayer.setName(event.getConnection().getName());
                        cloudPlayer.setFirstLogin(System.currentTimeMillis());
                        cloudPlayer.setConnected(true);
                        cloudPlayer.setLastLogin(System.currentTimeMillis());
                        cloudPlayer.setUniqueId(uniqueId);
                        cloudPlayer.setLastIp(event.getConnection().getAddress().getHostString());
                        cloudPlayer.setLastConnectedProxyId(ProxyCloudAPI.getInstance().getService().getUniqueId());
                        CloudAPI.getInstance().getPlayerManager().createPlayerAsync(cloudPlayer)
                                .onFailure(throwable -> {
                                    event.setCancelled(true);
                                    event.completeIntent(ProxyCloudAPI.getInstance().getPlugin());
                                    event.getConnection().disconnect(throwable.getMessage());
                                    CloudAPI.getInstance().getConsole().error("Error while creating player " + event.getConnection().getUniqueId() + "!", throwable);
                                }).onSuccess(c -> {
                                    event.completeIntent(ProxyCloudAPI.getInstance().getPlugin());
                                });
                        return;
                    }
                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(uniqueId)
                            .onFailure(throwable -> {
                                event.setCancelled(true);
                                event.completeIntent(ProxyCloudAPI.getInstance().getPlugin());
                                CloudAPI.getInstance().getConsole().error("Error while getting player " + event.getConnection().getUniqueId() + "!", throwable);
                                event.getConnection().disconnect(throwable.getMessage());
                            }).onSuccess(holder -> {
                                holder.getImpl(CloudPlayer.class).setConnected(true);
                                holder.getImpl(CloudPlayer.class).setLastLogin(System.currentTimeMillis());
                                holder.getImpl(CloudPlayer.class).setLastConnectedProxyId(ProxyCloudAPI.getInstance().getService().getUniqueId());
                                holder.getImpl(CloudPlayer.class).setName(event.getConnection().getName());
                                holder.get().updateAsync()
                                        .onFailure(throwable -> {
                                            event.setCancelled(true);
                                            event.completeIntent(ProxyCloudAPI.getInstance().getPlugin());
                                            event.getConnection().disconnect(throwable.getMessage());
                                            CloudAPI.getInstance().getConsole().error("Error while updating player " + event.getConnection().getUniqueId() + "!", throwable);
                                        }).onSuccess(v -> {
                                            event.completeIntent(ProxyCloudAPI.getInstance().getPlugin());
                                        });
                            });
                });
    }

}
