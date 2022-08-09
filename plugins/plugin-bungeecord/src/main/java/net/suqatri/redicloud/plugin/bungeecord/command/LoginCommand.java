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
import net.suqatri.redicloud.api.impl.configuration.impl.PlayerConfiguration;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

@CommandAlias("login")
public class LoginCommand extends BaseCommand {

    @Default
    @Syntax("<Password>")
    @Description("Login to your account")
    public void onRegister(CommandSender commandSender, String password) {

        if(!(commandSender instanceof ProxiedPlayer)){
            commandSender.sendMessage("§cYou need to be a player to logout!");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        if(!player.getServer().getInfo().getName().startsWith("Verify")){
            player.sendMessage("§cYou need to be on the verify server to register!");
            return;
        }

        if(player.getPendingConnection().isOnlineMode()){
            player.sendMessage("§cYou are already logged in as a premium account!");
            ICloudService cloudService = CloudAPI.getInstance().getServiceManager().getFallbackService(player.hasPermission("redicloud.maintenance.bypass"));
            if(cloudService == null){
                player.disconnect("§cNo service available!");
                return;
            }
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(cloudService.getName());
            if(serverInfo == null){
                player.disconnect("§cNo service available!");
                return;
            }
            player.connect(serverInfo);
            return;
        }


        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(player.getUniqueId())
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check player", e))
                .onSuccess(exists -> {
                    if(!exists){
                        player.sendMessage("§cYou do not have an account!");
                        player.sendMessage("§c/register <Password> <Password>");
                        return;
                    }
                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getName())
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get player", e))
                            .onSuccess(cloudPlayer -> {

                                if(cloudPlayer.isLoggedIn()){
                                    player.sendMessage("You are already logged in as " + cloudPlayer.getName() + "!");
                                    return;
                                }

                                CloudPlayer impl = (CloudPlayer) cloudPlayer;

                                if(impl.getPasswordHash() == null){
                                    player.sendMessage("§cYou need to register first!");
                                    player.sendMessage("§c/register <Password> <Password>");
                                    return;
                                }

                                PlayerConfiguration configuration = BungeeCordCloudAPI.getInstance().getPlayerManager().getConfiguration();
                                if(!password.equals(password)) {
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

                                if(impl.getBcrypt().verifyHash(password, impl.getPasswordHash())){
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
                                }else{
                                    player.disconnect("§cWrong password!");
                                }
                            });
                });
    }

}
