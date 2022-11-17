package dev.redicloud.module.cloudflare;

import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.configuration.Configuration;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.module.CloudModule;
import dev.redicloud.module.cloudflare.configuration.CloudFlareConfiguration;
import dev.redicloud.module.cloudflare.configuration.CloudFlareDomainEntryConfiguration;
import dev.redicloud.module.cloudflare.configuration.CloudFlareGroupEntryConfiguration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class CloudFlareModule extends CloudModule {

    public static final String CONFIGURATION_KEY = "module-cloudflare";

    @Getter
    private static CloudFlareModule instance;

    private CloudFlareConfiguration configuration;
    private List<CloudFlareDomainAccess> domainAccesses;

    @Override
    public void onEnable() {

        if(getApi().getNetworkComponentInfo().getType() != NetworkComponentType.NODE) {
            CloudDefaultAPIImpl.getInstance().getModuleHandler().disableModule(this);
            return;
        }

        instance = this;

        if(!getApi().getConfigurationManager().existsConfiguration(CONFIGURATION_KEY)) {
            this.configuration = new CloudFlareConfiguration();
            this.configuration.getEntries().add(
                    new CloudFlareDomainEntryConfiguration
                            (false, "xxxxxxxxxxxxxxx", "123456", "example.com",
                                    Collections.singletonList(
                                            new CloudFlareGroupEntryConfiguration(
                                                    false, "Proxy", "@", 1, 1)))
            );
            getApi().getConfigurationManager().createConfiguration(this.configuration);
        }
        this.configuration = getApi().getConfigurationManager().getConfiguration(CONFIGURATION_KEY, CloudFlareConfiguration.class);

        if(!this.configuration.isEnabled()){
            CloudDefaultAPIImpl.getInstance().getModuleHandler().disableModule(this);
            return;
        }

        this.domainAccesses = new ArrayList<>();
        for (CloudFlareDomainEntryConfiguration entry : this.configuration.getEntries()) {
            CloudFlareDomainAccess domainAccess = new CloudFlareDomainAccess(entry);
            domainAccess.init();
            this.domainAccesses.add(domainAccess);
        }

    }

    @Override
    public void onDisable() {

        if(this.domainAccesses != null){
            for (CloudFlareDomainAccess domainAccess : this.domainAccesses) {
                domainAccess.close();
            }
        }

        instance = null;
    }
}
