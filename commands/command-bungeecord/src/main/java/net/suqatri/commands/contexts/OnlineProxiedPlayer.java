package net.suqatri.commands.contexts;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.commands.bungee.contexts.OnlinePlayer;

/**
 * @deprecated Use {@link OnlinePlayer}
 */
@Deprecated
public class OnlineProxiedPlayer extends OnlinePlayer {
    public OnlineProxiedPlayer(ProxiedPlayer player) {
        super(player);
    }
}
