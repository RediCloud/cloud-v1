package dev.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.commons.UUIDUtility;
import dev.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.util.Random;
import java.util.UUID;

public class LoginListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent event) {
        if(event.isCancelled()) return;

        InitialHandler initialHandler = (InitialHandler)event.getConnection();

        UUID uniqueId = event.getConnection().getUniqueId();
        String name = event.getConnection().getName();

        if(!initialHandler.isOnlineMode()){
            if(UUIDUtility.isPremium(uniqueId, name)){
                event.setCancelled(true);
                event.setCancelReason("You are a premium player. Please reconnect.");
                return;
            }
            return;
        }

        event.registerIntent(BungeeCordCloudAPI.getInstance().getPlugin());

        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(uniqueId)
                .onFailure(throwable -> {
                    event.setCancelled(true);
                    event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
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
                        cloudPlayer.setLastConnectedProxyId(BungeeCordCloudAPI.getInstance().getService().getUniqueId());
                        cloudPlayer.setCracked(!initialHandler.isOnlineMode());
                        cloudPlayer.setPasswordLogRounds(10 + new Random().nextInt(30 - 10 + 1));
                        CloudAPI.getInstance().getPlayerManager().createPlayerAsync(cloudPlayer)
                                .onFailure(throwable -> {
                                    event.setCancelled(true);
                                    event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                    event.getConnection().disconnect(throwable.getMessage());
                                    CloudAPI.getInstance().getConsole().error("Error while creating player " + event.getConnection().getUniqueId() + "!", throwable);
                                }).onSuccess(c -> {
                                    event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                });
                        return;
                    }
                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(uniqueId)
                            .onFailure(throwable -> {
                                event.setCancelled(true);
                                event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                CloudAPI.getInstance().getConsole().error("Error while getting player " + event.getConnection().getUniqueId() + "!", throwable);
                                event.getConnection().disconnect(throwable.getMessage());
                            }).onSuccess(o -> {
                                String oldName = o.getName();
                                CloudPlayer cloudPlayer = (CloudPlayer) o;
                                cloudPlayer.setConnected(true);
                                cloudPlayer.setLastLogin(System.currentTimeMillis());
                                cloudPlayer.setLastConnectedProxyId(BungeeCordCloudAPI.getInstance().getService().getUniqueId());
                                cloudPlayer.setName(event.getConnection().getName());
                                cloudPlayer.setCracked(!initialHandler.isOnlineMode());
                                cloudPlayer.updateAsync()
                                    .onFailure(throwable -> {
                                        event.setCancelled(true);
                                        event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                        event.getConnection().disconnect(throwable.getMessage());
                                        CloudAPI.getInstance().getConsole().error("Error while updating player " + event.getConnection().getUniqueId() + "!", throwable);
                                    }).onSuccess(v -> {
                                        event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                        if (!oldName.equalsIgnoreCase(cloudPlayer.getName())) {
                                            CloudAPI.getInstance().getPlayerManager().updateName(cloudPlayer.getUniqueId(), cloudPlayer.getName(), oldName);
                                        }
                                    });
                            });
                });

        }

}
