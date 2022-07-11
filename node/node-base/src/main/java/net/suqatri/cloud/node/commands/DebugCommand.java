package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.event.CloudEventInvoker;
import net.suqatri.cloud.api.impl.event.CloudEventManager;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Subcommand;

@CommandAlias("debug")
public class DebugCommand extends ConsoleCommand {

    @Subcommand("event listeners")
    public void onEventListener(CommandSender commandSender){
        commandSender.sendMessage("");
        commandSender.sendMessage("Event listeners:");
        ((CloudEventManager)CloudAPI.getInstance().getEventManager()).getByEventBaked().forEach((eventClass, invokers) -> {
            commandSender.sendMessage(" - " + eventClass.getSimpleName());
            for (CloudEventInvoker invoker : invokers) {
                commandSender.sendMessage("     -> Class" + invoker.getListener().getClass().getName());
                commandSender.sendMessage("     -> Methode" + invoker.getMethod().getName());
                commandSender.sendMessage("-----");
            }
            commandSender.sendMessage("_-----------------_");
        });
        commandSender.sendMessage("");
    }

}
