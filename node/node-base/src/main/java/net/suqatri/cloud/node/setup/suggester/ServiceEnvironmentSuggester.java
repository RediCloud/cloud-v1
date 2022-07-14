package net.suqatri.cloud.node.setup.suggester;

import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.node.console.setup.Setup;
import net.suqatri.cloud.node.console.setup.SetupEntry;
import net.suqatri.cloud.node.console.setup.SetupSuggester;

import java.util.Arrays;
import java.util.List;

public class ServiceEnvironmentSuggester implements SetupSuggester {

    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.stream(ServiceEnvironment.values()).parallel().map(ServiceEnvironment::name).collect(java.util.stream.Collectors.toList());
    }

}
