package net.suqatri.redicloud.plugin.bungeecord.command;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.commands.BaseCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;
import net.suqatri.commands.annotation.Description;
import net.suqatri.commands.annotation.Syntax;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.impl.player.PlayerConfiguration;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

@CommandAlias("logout")
public class LogoutCommand extends BaseCommand {

    @Default
    @Description("Destroy your session")
    public void onLogout(ProxiedPlayer player) {

        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getName())
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
    }

}
