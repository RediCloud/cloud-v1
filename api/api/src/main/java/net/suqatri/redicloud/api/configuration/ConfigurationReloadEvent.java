package net.suqatri.redicloud.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.suqatri.redicloud.api.event.CloudEvent;

@Getter @AllArgsConstructor
public class ConfigurationReloadEvent extends CloudEvent {

    private final IConfiguration configuration;

}
