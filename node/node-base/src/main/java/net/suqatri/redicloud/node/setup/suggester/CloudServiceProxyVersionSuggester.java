package net.suqatri.redicloud.node.setup.suggester;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupEntry;
import net.suqatri.redicloud.node.console.setup.SetupSuggester;

import java.util.List;
import java.util.stream.Collectors;

public class CloudServiceProxyVersionSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersions()
                .parallelStream()
                .filter(holder -> holder.get().getEnvironmentType() == ServiceEnvironment.BUNGEECORD
                        || holder.get().getEnvironmentType() == ServiceEnvironment.VELOCITY)
                .map(holder -> holder.get().getName())
                .collect(Collectors.toList());
    }
}
