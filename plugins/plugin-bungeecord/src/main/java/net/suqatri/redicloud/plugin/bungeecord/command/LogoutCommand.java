package net.suqatri.redicloud.plugin.bungeecord.command;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.commands.BaseCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;
import net.suqatri.commands.annotation.Description;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;

@CommandAlias("logout")
public class LogoutCommand extends BaseCommand {

    @Default
    @Description("Destroy your session")
    public void onLogout(ProxiedPlayer player) {

        if(player.getPendingConnection().isOnlineMode()){
            player.sendMessage("§cYou can't logout in online mode");
            return;
        }

        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(player.getUniqueId())
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check player", e))
                .onSuccess(exists -> {

                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getUniqueId())
                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get player", e))
                        .onSuccess(cloudPlayer -> {
                            if(!cloudPlayer.isLoggedIn()){
                                player.sendMessage("You are not logged in!");
                                return;
                            }

                            CloudPlayer impl = (CloudPlayer) cloudPlayer;
                            impl.setSessionStart(-1L);
                            impl.setSessionIp(null);
                            impl.updateAsync();

                            player.disconnect("§cYou have been logged out!");
                        });
                });
    }

}