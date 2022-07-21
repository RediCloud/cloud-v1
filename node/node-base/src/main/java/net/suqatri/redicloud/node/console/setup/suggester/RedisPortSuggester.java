package net.suqatri.redicloud.node.console.setup.suggester;

import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupEntry;
import net.suqatri.redicloud.node.console.setup.SetupSuggester;

import java.util.Collections;
import java.util.List;

public class RedisPortSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Collections.singletonList("6379");
    }
}
