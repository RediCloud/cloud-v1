package net.suqatri.redicloud.node.console.setup.suggester;

import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupEntry;
import net.suqatri.redicloud.node.console.setup.SetupSuggester;

import java.util.ArrayList;
import java.util.List;

public class IntegerSuggester implements SetupSuggester {

    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        List<String> integers = new ArrayList<>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            integers.add(String.valueOf(i));
        }
        return integers;
    }
}
