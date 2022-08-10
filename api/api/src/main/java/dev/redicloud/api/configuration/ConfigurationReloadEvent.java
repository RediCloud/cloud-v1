package dev.redicloud.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.redicloud.api.event.CloudEvent;

@Getter @AllArgsConstructor
public class ConfigurationReloadEvent extends CloudEvent {

    private final IConfiguration configuration;

}
