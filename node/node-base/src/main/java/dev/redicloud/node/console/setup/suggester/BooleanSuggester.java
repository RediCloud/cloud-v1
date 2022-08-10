package dev.redicloud.node.console.setup.suggester;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;

import java.util.Arrays;
import java.util.List;

public class BooleanSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.asList("true", "false");
    }
}
