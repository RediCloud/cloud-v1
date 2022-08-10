package dev.redicloud.node.setup.suggester;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;

import java.util.List;
import java.util.stream.Collectors;

public class CloudNodeSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return CloudAPI.getInstance().getNodeManager().getNodes().parallelStream()
                .map(holder -> holder.getName())
                .collect(Collectors.toList());
    }
}
