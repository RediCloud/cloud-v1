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

import java.util.Random;

@CommandAlias("register")
public class RegisterCommand extends BaseCommand {

    @Default
    @Syntax("<Password> <Password>")
    @Description("Register a new account")
    public void onRegister(CommandSender commandSender, String password, String password2) {

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
                .thenAcceptAsync(exists -> {
                    if(exists){
                        player.sendMessage("§cYou already have an account!");
                        player.sendMessage("§c/login <Password>");
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
                    if(password.toLowerCase().contains(player.getName().toLowerCase()) && configuration.isPasswordCanContainsPlayerName()){
                        player.sendMessage("§cPassword cannot contain your player name!");
                        return;
                    }

                    CloudPlayer cloudPlayer = new CloudPlayer();

                    cloudPlayer.setConnected(true);
                    cloudPlayer.setUniqueId(player.getUniqueId());
                    cloudPlayer.setName(player.getName());
                    cloudPlayer.setLastIp(player.getAddress().getAddress().getHostAddress());
                    cloudPlayer.setLastLogin(System.currentTimeMillis());
                    cloudPlayer.setLastConnectedProxyId(BungeeCordCloudAPI.getInstance().getService().getUniqueId());
                    cloudPlayer.setFirstLogin(System.currentTimeMillis());

                    cloudPlayer.setPasswordLogRounds(10 + new Random().nextInt(30 - 10 + 1));
                    cloudPlayer.setCracked(true);
                    cloudPlayer.setPassword(password);
                    cloudPlayer.setSessionIp(player.getAddress().getAddress().getHostAddress());

                    CloudAPI.getInstance().getConsole().debug("Registering player " + cloudPlayer.getName() + " with password-hash " + cloudPlayer.getPasswordHash());

                    CloudAPI.getInstance().getPlayerManager().createPlayerAsync(cloudPlayer)
                        .onFailure(e -> {
                            player.sendMessage("§cFailed to create player!");
                            CloudAPI.getInstance().getConsole().error("Failed to create player", e);
                        })
                        .thenAcceptAsync(registeredCloudPlayer -> {
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
                        });
                });
    }

}
