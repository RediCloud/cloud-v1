package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

public class LoginListener implements Listener {

    private static final String UUID_FIELD_NAME = "uniqueId";
    private static final MethodHandle uniqueIdSetter;

    static {
        MethodHandle setHandle = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Field uuidField = InitialHandler.class.getDeclaredField(UUID_FIELD_NAME);
            uuidField.setAccessible(true);
            setHandle = lookup.unreflectSetter(uuidField);
        } catch (ReflectiveOperationException exception) {
            exception.printStackTrace();
        }
        uniqueIdSetter = setHandle;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if(event.isCancelled()) return;

        UUID uniqueId = event.getConnection().getUniqueId();

        event.registerIntent(BungeeCordCloudAPI.getInstance().getPlugin());

        if(event.getConnection().isOnlineMode()) {

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
                            cloudPlayer.setCracked(false);
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
                                    cloudPlayer.updateAsync()
                                        .onFailure(throwable -> {
                                            event.setCancelled(true);
                                            event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                            event.getConnection().disconnect(throwable.getMessage());
                                            CloudAPI.getInstance().getConsole().error("Error while updating player " + event.getConnection().getUniqueId() + "!", throwable);
                                        }).onSuccess(v -> {
                                            event.completeIntent(BungeeCordCloudAPI.getInstance().getPlugin());
                                            if(!oldName.equalsIgnoreCase(cloudPlayer.getName())){
                                                CloudAPI.getInstance().getPlayerManager().updateName(cloudPlayer.getUniqueId(), cloudPlayer.getName(), oldName);
                                            }
                                        });
                                });
                    });
        }else {

        }
    }

}
