package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.event.CloudEventInvoker;
import net.suqatri.cloud.api.impl.event.CloudEventManager;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.ConsoleCommandExecutionContext;
import net.suqatri.commands.InvalidCommandArgument;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Subcommand;
import net.suqatri.commands.contexts.ContextResolver;

import java.lang.reflect.Method;

@CommandAlias("debug")
public class DebugCommand extends ConsoleCommand {

    @Subcommand("file-transfer sent")
    public void onFileTransferSent(CommandSender commandSender){
        commandSender.sendMessage("Sent process queue size: " + NodeLauncher.getInstance().getFileTransferManager().getThread().getSentProcesses().size());
    }

    @Subcommand("file-transfer received")
    public void onFileTransferReceived(CommandSender commandSender){
        commandSender.sendMessage("Received process queue size: " + NodeLauncher.getInstance().getFileTransferManager().getThread().getReceiveProcesses().size());
    }



}
