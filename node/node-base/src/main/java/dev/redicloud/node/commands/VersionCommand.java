package dev.redicloud.node.commands;

import dev.redicloud.commands.CommandSender;
import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.node.NodeLauncher;

@CommandAlias("version|info|ver|v")
public class VersionCommand extends ConsoleCommand {

    @Default
    public void onVersion(CommandSender commandSender) {
        NodeLauncher.getInstance().getConsole().printCloudHeader(false);
    }

}
