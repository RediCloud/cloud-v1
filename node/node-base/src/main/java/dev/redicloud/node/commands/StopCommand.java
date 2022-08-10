package dev.redicloud.node.commands;

import dev.redicloud.commands.CommandSender;
import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.node.NodeLauncher;

import java.util.concurrent.TimeUnit;

@CommandAlias("stop|shutdown")
public class StopCommand extends ConsoleCommand {

    private long lastTime = 0L;

    @Default
    public void onStop(CommandSender commandSender) {
        if ((System.currentTimeMillis() - this.lastTime) < TimeUnit.SECONDS.toMillis(5)) {
            commandSender.sendMessage("§cShutdown node by command...");
            NodeLauncher.getInstance().shutdown(false);
            return;
        }
        this.lastTime = System.currentTimeMillis();
        commandSender.sendMessage("Enter %hc'stop'%tc in the next 10 seconds again to shutdown the node.");
    }

}
