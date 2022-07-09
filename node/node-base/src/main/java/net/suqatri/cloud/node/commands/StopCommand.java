package net.suqatri.cloud.node.commands;

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
        if((System.currentTimeMillis() - lastTime) < TimeUnit.SECONDS.toMillis(5)){

        }
    }

}
