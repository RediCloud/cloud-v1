package dev.redicloud.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.redicloud.commands.BaseCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.commands.annotation.Description;
import dev.redicloud.commands.annotation.Syntax;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.configuration.impl.PlayerConfiguration;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;

import java.util.Optional;

@CommandAlias("login")
public class LoginCommand extends BaseCommand {

    @Default
    @Syntax("<Password>")
    @Description("Login to your account")
    public void onRegister(CommandSource commandSender, String password) {

        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(LegacyMessageUtils.component("You need to be a player to logout!"));
            return;
        }

        Player player = (Player) commandSender;

        if(!player.getCurrentServer().get().getServerInfo().getName().startsWith("Verify")){
            player.sendMessage(LegacyMessageUtils.component("§cYou need to be on the verify server to register!"));
            return;
        }

        if(player.isOnlineMode()){
            player.sendMessage(LegacyMessageUtils.component("§cYou are already logged in as a premium account!"));
            ICloudService cloudService = CloudAPI.getInstance().getServiceManager().getFallbackService(player.hasPermission("redicloud.maintenance.bypass"));
            if(cloudService == null){
                player.disconnect(LegacyMessageUtils.component("§cNo service available!"));
                return;
            }
            Optional<RegisteredServer> registeredServer = VelocityCloudAPI.getInstance().getProxyServer().getServer(cloudService.getName());
            if(!registeredServer.isPresent()){
                player.disconnect(LegacyMessageUtils.component("§cNo service available!"));
                return;
            }
            player.createConnectionRequest(registeredServer.get()).connect();
            return;
        }

        CloudAPI.getInstance().getPlayerManager().existsPlayerAsync(player.getUniqueId())
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check player", e))
                .onSuccess(exists -> {
                    if(!exists){
                        player.sendMessage(LegacyMessageUtils.component("§cYou do not have an account!"));
                        player.sendMessage(LegacyMessageUtils.component("§c/register <Password> <Password>"));
                        return;
                    }
                    CloudAPI.getInstance().getPlayerManager().getPlayerAsync(player.getUniqueId())
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get player", e))
                            .thenAcceptAsync(cloudPlayer -> {

                                if(cloudPlayer.isLoggedIn()){
                                    player.sendMessage(LegacyMessageUtils.component("You are already logged in as " + cloudPlayer.getName() + "!"));
                                    return;
                                }

                                CloudPlayer impl = (CloudPlayer) cloudPlayer;

                                if(impl.getPasswordHash() == null){
                                    player.sendMessage(LegacyMessageUtils.component("§cYou need to register first!"));
                                    player.sendMessage(LegacyMessageUtils.component("§c/register <Password> <Password>"));
                                    return;
                                }

                                PlayerConfiguration configuration = VelocityCloudAPI.getInstance().getPlayerManager().getConfiguration();

                                if(impl.getBcrypt().verifyHash(password, impl.getPasswordHash())){
                                    impl.setSessionIp(player.getRemoteAddress().getAddress().getHostAddress());
                                    impl.setLastLogin(System.currentTimeMillis());
                                    impl.setConnected(true);
                                    impl.setLastIp(player.getRemoteAddress().getAddress().getHostAddress());
                                    impl.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
                                    impl.updateAsync();
                                    player.sendMessage(LegacyMessageUtils.component("§aYou are now logged in as " + impl.getName() + "!"));
                                    ICloudService fallback = CloudAPI.getInstance().getServiceManager().getFallbackService(cloudPlayer);
                                    if(fallback == null){
                                        player.disconnect(LegacyMessageUtils.component("No fallback service available!"));
                                        return;
                                    }
                                    Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(fallback.getServiceName());
                                    if(!serverInfo.isPresent()){
                                        player.disconnect(LegacyMessageUtils.component("No fallback service available!"));
                                        CloudAPI.getInstance().getConsole().warn("Service " + fallback.getServiceName() + " is not registered as proxy service!");
                                        return;
                                    }
                                    player.createConnectionRequest(serverInfo.get()).connect();
                                }else{
                                    player.disconnect(LegacyMessageUtils.component("§cIncorrect password!"));
                                }
                            });
                });
    }

}
