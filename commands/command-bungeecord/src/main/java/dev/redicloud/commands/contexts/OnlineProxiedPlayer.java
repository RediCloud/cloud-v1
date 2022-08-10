package dev.redicloud.commands.contexts;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import dev.redicloud.commands.bungee.contexts.OnlinePlayer;

/**
 * @deprecated Use {@link OnlinePlayer}
 */
@Deprecated
public class OnlineProxiedPlayer extends OnlinePlayer {
    public OnlineProxiedPlayer(ProxiedPlayer player) {
        super(player);
    }
}
