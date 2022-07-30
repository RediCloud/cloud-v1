package net.suqatri.redicloud.node.setup.redis;

import lombok.Getter;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.AcceptEmptyInput;
import net.suqatri.redicloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.suggester.HostNameSuggester;
import net.suqatri.redicloud.node.console.setup.suggester.RedisPortSuggester;

@Getter
public class RedisSetup extends Setup<RedisSetup> {

    @Question(id = 1, question = "What´s the hostname of the Redis server?")
    @AnswerCompleter(value = HostNameSuggester.class)
    private String hostname;

    @Question(id = 2, question = "What´s the port of the Redis server?")
    @AnswerCompleter(value = RedisPortSuggester.class)
    private int port;

    @AcceptEmptyInput
    @Question(id = 3, question = "What´s the password of the Redis server?")
    private String password;

    @Question(id = 4, question = "What´s the database id of the Redis server?")
    private int databaseId;

    public RedisSetup() {
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
