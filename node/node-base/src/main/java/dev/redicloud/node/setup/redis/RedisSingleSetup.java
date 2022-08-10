package dev.redicloud.node.setup.redis;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.AnswerCompleter;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.suggester.HostNameSuggester;
import dev.redicloud.node.console.setup.suggester.RedisPortSuggester;
import lombok.Getter;
import dev.redicloud.node.NodeLauncher;

@Getter
public class RedisSingleSetup extends Setup<RedisSingleSetup> {

    @Question(id = 1, question = "What´s the hostname of the Redis server?")
    @AnswerCompleter(value = HostNameSuggester.class)
    private String hostname;

    @Question(id = 2, question = "What´s the port of the Redis server?")
    @AnswerCompleter(value = RedisPortSuggester.class)
    private int port;

    @Question(id = 3, question = "What´s the database id of the Redis server?")
    private int databaseId;

    public RedisSingleSetup() {
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
