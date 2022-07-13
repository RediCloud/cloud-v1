package net.suqatri.cloud.node.setup.group;

import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.Setup;

public class GroupSetup extends Setup<GroupSetup> {


    public GroupSetup() {
        super(NodeLauncher.getInstance().getConsole());
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
