package net.suqatri.redicloud.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.commands.BaseCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;
import net.suqatri.commands.annotation.Description;
import net.suqatri.commands.annotation.Syntax;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.configuration.impl.PlayerConfiguration;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;

import java.util.Optional;
import java.util.Random;

@CommandAlias("register")
public class RegisterCommand extends BaseCommand {

    @Default
    @Syntax("<Password> <Password>")
    @Description("Register a new account")
    public void onRegister(CommandSource commandSender, String password, String password2) {

        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(LegacyMessageUtils.component("§cYou must be a player to use this command"));
            return;
        }

        Player player = (Player) commandSender;

        if(!player.getCurrentServer().get().getServerInfo().getName().startsWith("Verify")){
            player.sendMessage(LegacyMessageUtils.component("&cYou must be on the verify server to use this command"));
            return;
        }

        if(player.isOnlineMode()){
            player.sendMessage(LegacyMessageUtils.component("§cYou are already logged in as a premium account!"));
            ICloudService cloudService = CloudAPI.getInstance().getServiceManager().getFallbackService(player.hasPermission("redicloud.maintenance.bypass"));
            if(cloudService == null){
                player.disconnect(LegacyMessageUtils.component("§cNo service available!"));
                return;
            }
            Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(cloudService.getName());
            if(!serverInfo.isPresent()){
                player.disconnect(LegacyMessageUtils.component("§cNo service available!"));
                return;
            }
            player.createConnectionRequest(serverInfo.get()).connect();
            return;
        }

        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(player.getUniqueId())
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check player", e))
                .thenAcceptAsync(exists -> {
                    if(exists){
                        player.sendMessage(LegacyMessageUtils.component("§cYou are already registered!"));
                        player.sendMessage(LegacyMessageUtils.component("§c/login <Password>"));
                        return;
                    }

                    PlayerConfiguration configuration = VelocityCloudAPI.getInstance().getPlayerManager().getConfiguration();
                    if(!password.equals(password2)) {
                        player.sendMessage(LegacyMessageUtils.component("§cPasswords do not match!"));
                        return;
                    }
                    if(password.length() < configuration.getMinPasswordLength()) {
                        player.sendMessage(LegacyMessageUtils.component("§cPassword is too short! Minimum length is " + configuration.getMinPasswordLength() + " characters."));
                        return;
                    }
                    if(password.length() > configuration.getMaxPasswordLength()) {
                        player.sendMessage(LegacyMessageUtils.component("§cPassword is too long! Maximum length is " + configuration.getMaxPasswordLength() + " characters."));
                        return;
                    }
                    if(password.toLowerCase().contains(player.getUsername().toLowerCase()) && configuration.isPasswordCanContainsPlayerName()){
                        player.sendMessage(LegacyMessageUtils.component("§cPassword cannot contain your player name!"));
                        return;
                    }

                    CloudPlayer cloudPlayer = new CloudPlayer();

                    cloudPlayer.setConnected(true);
                    cloudPlayer.setUniqueId(player.getUniqueId());
                    cloudPlayer.setName(player.getUsername());
                    cloudPlayer.setLastIp(player.getRemoteAddress().getAddress().getHostAddress());
                    cloudPlayer.setLastLogin(System.currentTimeMillis());
                    cloudPlayer.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
                    cloudPlayer.setFirstLogin(System.currentTimeMillis());

                    cloudPlayer.setPasswordLogRounds(10 + new Random().nextInt(30 - 10 + 1));
                    cloudPlayer.setCracked(true);
                    cloudPlayer.setPassword(password);
                    cloudPlayer.setSessionIp(player.getRemoteAddress().getAddress().getHostAddress());

                    CloudAPI.getInstance().getConsole().debug("Registering player " + cloudPlayer.getName() + " with password-hash " + cloudPlayer.getPasswordHash());

                    CloudAPI.getInstance().getPlayerManager().createPlayerAsync(cloudPlayer)
                        .onFailure(e -> {
                            player.sendMessage(LegacyMessageUtils.component("§cFailed to register player!"));
                            CloudAPI.getInstance().getConsole().error("Failed to create player", e);
                        })
                        .thenAcceptAsync(registeredCloudPlayer -> {
                            player.sendMessage(LegacyMessageUtils.component("§aSuccessfully logged in as " + cloudPlayer.getName() + "!"));
                            ICloudService fallback = CloudAPI.getInstance().getServiceManager().getFallbackService(cloudPlayer);
                            if(fallback == null){
                                player.disconnect(LegacyMessageUtils.component("§cNo service available!"));
                                return;
                            }
                            Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(fallback.getServiceName());
                            if(!serverInfo.isPresent()){
                                player.disconnect(LegacyMessageUtils.component("No fallback service available!"));
                                CloudAPI.getInstance().getConsole().warn("Service " + fallback.getServiceName() + " is not registered as proxy service!");
                                return;
                            }
                            player.createConnectionRequest(serverInfo.get()).connect();
                        });
                });
    }

}
