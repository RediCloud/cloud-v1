package dev.redicloud.node.setup.suggester;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;
import dev.redicloud.api.redis.RedisType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedisTypeSuggester implements SetupSuggester {

    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.stream(RedisType.values()).parallel().map(RedisType::name).collect(Collectors.toList());
    }

}
