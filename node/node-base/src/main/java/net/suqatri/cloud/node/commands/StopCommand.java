package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;

import java.util.concurrent.TimeUnit;

@CommandAlias("stop|shutdown")
public class StopCommand extends ConsoleCommand {

    private long lastTime = 0L;

    @Default
    public void onStop(CommandSender commandSender){
        if((System.currentTimeMillis() - this.lastTime) < TimeUnit.SECONDS.toMillis(5)){
            System.out.println("Shutdown node by command...");
            NodeLauncher.getInstance().shutdown();
            return;
        }
        this.lastTime = System.currentTimeMillis();
        commandSender.sendMessage("Enter 'stop' in the next 10 seconds again to shutdown the node.");
    }

}
