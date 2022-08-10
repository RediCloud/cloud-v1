package dev.redicloud.plugin.bungeecord.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import dev.redicloud.commands.BaseCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.commands.annotation.Description;
import dev.redicloud.commands.annotation.Syntax;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.impl.configuration.impl.PlayerConfiguration;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

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
                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getUniqueId())
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get player", e))
                            .thenAcceptAsync(cloudPlayer -> {

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

                                if(impl.getBcrypt().verifyHash(password, impl.getPasswordHash())){
                                    impl.setSessionIp(player.getAddress().getAddress().getHostAddress());
                                    impl.setLastLogin(System.currentTimeMillis());
                                    impl.setConnected(true);
                                    impl.setLastIp(player.getAddress().getAddress().getHostAddress());
                                    impl.setLastConnectedProxyId(BungeeCordCloudAPI.getInstance().getService().getUniqueId());
                                    impl.updateAsync();
                                    player.sendMessage("§aSuccessfully logged in as " + cloudPlayer.getName() + "!");
                                    ICloudService fallback = CloudAPI.getInstance().getServiceManager().getFallbackService(cloudPlayer);
                                    if(fallback == null){
                                        player.disconnect("No fallback service available!");
                                        return;
                                    }
                                    ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(fallback.getServiceName());
                                    if(serverInfo == null){
                                        player.disconnect("No fallback service available!");
                                        CloudAPI.getInstance().getConsole().warn("Service " + fallback.getServiceName() + " is not registered as proxy service!");
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
