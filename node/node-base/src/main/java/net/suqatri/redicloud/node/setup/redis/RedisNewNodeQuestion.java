package net.suqatri.redicloud.node.setup.redis;

import lombok.Getter;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.NodeConsole;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.suggester.BooleanSuggester;

@Getter
public class RedisNewNodeQuestion extends Setup<RedisNewNodeQuestion> {

    @Question(id = 1, question = "Do you want to add a new node to the cluster?")
    @AnswerCompleter(value = BooleanSuggester.class)
    private boolean addNewNode;

    public RedisNewNodeQuestion() {
        super(NodeLauncher.getInstance().getConsole());
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return false;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.CLEAR_SCREEN_AFTER;
    }
}
