package dev.redicloud.node.setup.redis;

import lombok.Getter;
import dev.redicloud.api.redis.RedisType;
import dev.redicloud.node.NodeLauncher;
import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.AcceptEmptyInput;
import dev.redicloud.node.console.setup.annotations.AnswerCompleter;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.annotations.RequiresEnum;
import dev.redicloud.node.setup.suggester.RedisTypeSuggester;

@Getter
public class RedisGenerellSetup extends Setup<RedisGenerellSetup> {

    @Question(id = 1, question = "What type of redis server do you want to use?")
    @RequiresEnum(RedisType.class)
    @AnswerCompleter(value = RedisTypeSuggester.class)
    private RedisType redisType;

    @AcceptEmptyInput
    @Question(id = 2, question = "What is the password of the redis server/cluster?")
    private String password;

    public RedisGenerellSetup() {
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
        return SetupHeaderBehaviour.NOTHING;
    }
}
