package net.suqatri.commands.contexts;

import net.suqatri.commands.bungee.contexts.OnlinePlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @deprecated Use {@link OnlinePlayer}
 */
@Deprecated
public class OnlineProxiedPlayer extends OnlinePlayer {
    public OnlineProxiedPlayer(ProxiedPlayer player) {
        super(player);
    }
}
