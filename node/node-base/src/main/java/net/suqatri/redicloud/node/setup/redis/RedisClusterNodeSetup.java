package net.suqatri.redicloud.node.setup.redis;

import lombok.Getter;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.Question;

@Getter
public class RedisClusterNodeSetup extends Setup<RedisClusterNodeSetup> {

    @Question(id = 1, question = "What is the hostname of the cluster?")
    public String hostName;

    @Question(id = 2, question = "What is the port of the cluster?")
    public int port;

    private final boolean cancelable;

    public RedisClusterNodeSetup(boolean cancelable) {
        super(NodeLauncher.getInstance().getConsole());
        this.cancelable = cancelable;
    }

    @Override
    public boolean isCancellable() {
        return cancelable;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }
}
