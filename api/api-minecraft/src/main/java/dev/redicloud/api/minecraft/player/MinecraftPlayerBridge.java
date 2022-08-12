package dev.redicloud.api.minecraft.player;

import dev.redicloud.api.impl.player.RequestPlayerBridge;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.player.ICloudPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MinecraftPlayerBridge extends RequestPlayerBridge {

    public MinecraftPlayerBridge(ICloudPlayer player) {
        super(player);
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        Player player = Bukkit.getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isOnline()) {
            if(this.getPlayer().getLastConnectedServerId()
                    .equals(CloudAPI.getInstance().getNetworkComponentInfo().getIdentifier())
                    && this.getPlayer().isConnected()){
                super.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
            }
            return;
        }
        //TODO: send title to player
    }

    @Override
    public void sendActionbar(String message) {
        Player player = Bukkit.getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isOnline()) {
            if(this.getPlayer().getLastConnectedServerId()
                    .equals(CloudAPI.getInstance().getNetworkComponentInfo().getIdentifier())
                    && this.getPlayer().isConnected()){
                super.sendActionbar(message);
            }
            return;
        }
        //TODO: send actionbar to player
    }

    @Override
    public boolean hasPermission(String permission) {
        Player player = Bukkit.getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isOnline()) return player.hasPermission(permission);
        return false;
    }
}
