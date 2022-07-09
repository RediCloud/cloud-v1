package net.suqatri.cloud.node.setup.redis;

import lombok.Getter;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.Setup;
import net.suqatri.cloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.cloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.cloud.node.console.setup.annotations.Question;
import net.suqatri.cloud.node.console.setup.suggester.HostNameSuggester;
import net.suqatri.cloud.node.console.setup.suggester.RedisPortSuggester;

@Getter
public class RedisSetup extends Setup<RedisSetup> {

    @Question(id = 1, question = "What´s the hostname of the Redis server?")
    @AnswerCompleter(value = HostNameSuggester.class)
    private String hostname;

    @Question(id = 2, question = "What´s the port of the Redis server?")
    @AnswerCompleter(value = RedisPortSuggester.class)
    private int port;

    @Question(id = 3, question = "What´s the password of the Redis server?")
    private String password;

    @Question(id = 4, question = "What´s the database id of the Redis server?")
    private int databaseId;

    public RedisSetup(NodeConsole console) {
        super(console);
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
