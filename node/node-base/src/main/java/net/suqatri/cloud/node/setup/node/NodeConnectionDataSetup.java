package net.suqatri.cloud.node.setup.node;

import lombok.Getter;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.Setup;
import net.suqatri.cloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.cloud.node.console.setup.SetupSuggester;
import net.suqatri.cloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.cloud.node.console.setup.annotations.Answers;
import net.suqatri.cloud.node.console.setup.annotations.Question;
import net.suqatri.cloud.node.console.setup.annotations.QuestionTip;
import net.suqatri.cloud.node.console.setup.suggester.HostNameSuggester;
import net.suqatri.cloud.node.console.setup.suggester.MemorySuggester;

import java.util.Collections;
import java.util.UUID;

@Getter
public class NodeConnectionDataSetup extends Setup<NodeConnectionDataSetup> {

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

    private UUID uniqueId = UUID.randomUUID();

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
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }

}
