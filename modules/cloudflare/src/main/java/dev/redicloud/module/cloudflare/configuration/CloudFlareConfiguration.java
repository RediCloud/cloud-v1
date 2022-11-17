package dev.redicloud.module.cloudflare.configuration;

import dev.redicloud.api.impl.configuration.Configuration;
import dev.redicloud.module.cloudflare.CloudFlareModule;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CloudFlareConfiguration extends Configuration {

    private boolean enabled = false;
    private List<CloudFlareDomainEntryConfiguration> entries = new ArrayList<>();

    @Override
    public String getIdentifier() {
        return CloudFlareModule.CONFIGURATION_KEY;
    }

}
