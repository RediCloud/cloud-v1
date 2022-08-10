package dev.redicloud.node.setup.suggester;

import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceEnvironmentSuggester implements SetupSuggester {

    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.stream(ServiceEnvironment.values()).parallel().map(ServiceEnvironment::name).collect(Collectors.toList());
    }

}
