package net.suqatri.redicloud.node.setup.redis;

import lombok.Getter;
import net.suqatri.redicloud.api.redis.RedisType;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.AcceptEmptyInput;
import net.suqatri.redicloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.annotations.RequiresEnum;
import net.suqatri.redicloud.node.setup.suggester.RedisTypeSuggester;

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
        return SetupHeaderBehaviour.CLEAR_SCREEN_AFTER;
    }
}
