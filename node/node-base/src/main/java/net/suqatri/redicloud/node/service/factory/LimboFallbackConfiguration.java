package net.suqatri.redicloud.node.service.factory;

import lombok.Data;
import net.suqatri.redicloud.api.impl.configuration.Configuration;

@Data
public class LimboFallbackConfiguration extends Configuration {

    private String identifier = "limbo-fallbacks";
    private boolean enabled = true;

}
