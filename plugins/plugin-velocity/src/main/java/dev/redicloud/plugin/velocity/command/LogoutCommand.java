package dev.redicloud.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.redicloud.commands.BaseCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.commands.annotation.Description;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;

@CommandAlias("logout")
public class LogoutCommand extends BaseCommand {

    @Default
    @Description("Destroy your session")
    public void onLogout(CommandSource commandSender) {

        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(LegacyMessageUtils.component("§cYou need to be a player to logout!"));
            return;
        }

        Player player = (Player) commandSender;

        if(player.isOnlineMode()){
            player.sendMessage(LegacyMessageUtils.component("§cYou can't logout in online mode"));
            return;
        }

        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(player.getUniqueId())
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check player", e))
                .onSuccess(exists -> {

                    if(!exists){
                        player.sendMessage(LegacyMessageUtils.component("§cYou do not have an account!"));
                        return;
                    }

                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getUniqueId())
                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get player", e))
                        .onSuccess(cloudPlayer -> {
                            if(!cloudPlayer.isLoggedIn()){
                                player.sendMessage(LegacyMessageUtils.component("You are not logged in!"));
                                return;
                            }

                            CloudPlayer impl = (CloudPlayer) cloudPlayer;
                            impl.setSessionIp(null);
                            impl.updateAsync();

                            player.disconnect(LegacyMessageUtils.component("§cYou have been logged out!"));
                        });
                });
    }

}
