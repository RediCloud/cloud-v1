package net.suqatri.redicloud.plugin.bungeecord.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.commands.BaseCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;
import net.suqatri.commands.annotation.Description;
import net.suqatri.commands.annotation.Syntax;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.impl.player.PlayerConfiguration;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

@CommandAlias("register")
public class RegisterCommand extends BaseCommand {

    @Default
    @Syntax("<Password> <Password>")
    @Description("Register a new account")
    public void onRegister(ProxiedPlayer player, String password, String password2) {

        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getName())
            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get player", e))
            .onSuccess(cloudPlayer -> {

                if(cloudPlayer.isLoggedIn()){
                    player.sendMessage("You are already logged in as " + cloudPlayer.getName() + "!");
                    return;
                }

                PlayerConfiguration configuration = BungeeCordCloudAPI.getInstance().getPlayerManager().getConfiguration();
                if(!password.equals(password2)) {
                    player.sendMessage("§cPasswords do not match!");
                    return;
                }
                if(password.length() < configuration.getMinPasswordLength()) {
                    player.sendMessage("§cPassword is too short! Minimum length is " + configuration.getMinPasswordLength() + " characters.");
                    return;
                }
                if(password.length() > configuration.getMaxPasswordLength()) {
                    player.sendMessage("§cPassword is too long! Maximum length is " + configuration.getMaxPasswordLength() + " characters.");
                    return;
                }

                CloudPlayer impl = (CloudPlayer) cloudPlayer;

                String hash = impl.getBcrypt().hash(password);
                impl.setPasswordHash(hash);
                impl.setSessionStart(System.currentTimeMillis());
                impl.setSessionIp(player.getAddress().getAddress().getHostAddress());
                impl.updateAsync();

                player.sendMessage("§aSuccessfully logged in as " + cloudPlayer.getName() + "!");
                ICloudService fallback = CloudAPI.getInstance().getServiceManager().getFallbackService(cloudPlayer);
                if(fallback == null){
                    player.disconnect("No fallback service available!");
                    return;
                }
                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(fallback.getName());
                if(serverInfo == null){
                    player.disconnect("No fallback service available!");
                    CloudAPI.getInstance().getConsole().error("Service " + fallback.getName() + " is not registered as proxy service!");
                    return;
                }
                player.connect(serverInfo);
            });
    }

}
