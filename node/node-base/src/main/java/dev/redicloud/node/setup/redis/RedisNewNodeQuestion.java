package dev.redicloud.node.setup.redis;

import lombok.Getter;
import dev.redicloud.node.NodeLauncher;
import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.AnswerCompleter;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.suggester.BooleanSuggester;

@Getter
public class RedisNewNodeQuestion extends Setup<RedisNewNodeQuestion> {

    @Question(id = 1, question = "Do you want to add a new node to the cluster?")
    @AnswerCompleter(value = BooleanSuggester.class)
    private boolean addNewNode = true;

    public RedisNewNodeQuestion() {
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

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return this.addNewNode ?  SetupHeaderBehaviour.NOTHING : SetupHeaderBehaviour.CLEAR_SCREEN_AFTER;
    }
}
