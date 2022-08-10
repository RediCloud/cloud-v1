package dev.redicloud.node.setup.suggester;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;

import java.util.List;
import java.util.stream.Collectors;

public class CloudServiceProxyVersionSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .filter(holder -> holder.getEnvironmentType() == ServiceEnvironment.BUNGEECORD
                        || holder.getEnvironmentType() == ServiceEnvironment.VELOCITY)
                .map(holder -> holder.getName())
                .collect(Collectors.toList());
    }
}
