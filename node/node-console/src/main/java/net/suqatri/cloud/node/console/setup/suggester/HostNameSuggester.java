package net.suqatri.cloud.node.console.setup.suggester;

import net.suqatri.cloud.commons.connection.IPUtils;
import net.suqatri.cloud.node.console.setup.Setup;
import net.suqatri.cloud.node.console.setup.SetupEntry;
import net.suqatri.cloud.node.console.setup.SetupSuggester;

import java.util.Arrays;
import java.util.List;

public class HostNameSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.asList("127.0.0.1", IPUtils.getPublicIP());
    }
}
