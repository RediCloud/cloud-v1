package net.suqatri.redicloud.node.console.setup;

import java.util.List;

public interface SetupSuggester {

    List<String> suggest(Setup<?> setup, SetupEntry entry);

}
