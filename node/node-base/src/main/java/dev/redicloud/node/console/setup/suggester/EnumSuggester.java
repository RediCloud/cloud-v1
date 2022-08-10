package dev.redicloud.node.console.setup.suggester;

import dev.redicloud.node.console.setup.annotations.RequiresEnum;
import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        RequiresEnum requiresEnum = entry.getRequiresEnum();
        if (requiresEnum == null) return new ArrayList<>();
        Class<? extends Enum<?>> value = requiresEnum.value();
        return Arrays.stream(value.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
    }
}
