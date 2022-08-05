package net.suqatri.redicloud.node.setup.node;

import lombok.Getter;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.suggester.HostNameSuggester;
import net.suqatri.redicloud.node.console.setup.suggester.MemorySuggester;

import java.util.UUID;

@Getter
public class NodeConnectionDataSetup extends Setup<NodeConnectionDataSetup> {

    private final UUID uniqueId = UUID.randomUUID();
    @Question(id = 1, question = "What´s the name of this node?")
    @AnswerCompleter(value = HostNameSuggester.class)
    private String name;
    @Question(id = 2, question = "What´s the maximum memory of this node?")
    @AnswerCompleter(value = MemorySuggester.class)
    private int maxMemory;
    @Question(id = 3, question = "What´s the maximum started service count of this node?")
    private int maxServiceCount;
    @Question(id = 4, question = "What´s the maximum starting parallel service count of this node?")
    private int maxParallelServiceCount;
    @Question(id = 5, question = "Should this node be used to push files to the cluster?")
    private boolean fileNode;

    public NodeConnectionDataSetup() {
        super(NodeLauncher.getInstance().getConsole());
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.CLEAR_SCREEN_AFTER;
    }

}
