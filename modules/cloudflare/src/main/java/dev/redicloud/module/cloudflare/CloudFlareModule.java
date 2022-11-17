package dev.redicloud.module.cloudflare;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.module.CloudModule;
import dev.redicloud.module.cloudflare.configuration.CloudFlareConfiguration;
import dev.redicloud.module.cloudflare.configuration.CloudFlareDomainEntryConfiguration;
import dev.redicloud.module.cloudflare.configuration.CloudFlareGroupEntryConfiguration;
import dev.redicloud.module.cloudflare.domain.CloudFlareDomainAccess;
import dev.redicloud.module.cloudflare.listener.CloudServiceStartedListener;
import dev.redicloud.module.cloudflare.listener.CloudServiceStoppedListener;
import eu.roboflax.cloudflare.CloudflareRequest;
import eu.roboflax.cloudflare.CloudflareResponse;
import eu.roboflax.cloudflare.constants.Category;
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

        String nodeHost = getApi().getNetworkComponentInfo().getAsNode().getHostname();

        this.domainAccesses = new ArrayList<>();
        for (CloudFlareDomainEntryConfiguration entry : this.configuration.getEntries()) {
            CloudFlareDomainAccess domainAccess = new CloudFlareDomainAccess(entry, this);
            domainAccess.init();
            this.domainAccesses.add(domainAccess);

            domainAccess.existsNodeRecord().onFailure(throwable -> {
                    getApi().getConsole().error("Failed to check if node record exists for domain " + entry.getDomainName() + "!", throwable);
                }).onSuccess(exists -> {
                    if(exists) {
                        domainAccess.updateNodeRecord().onFailure(throwable -> {
                            getApi().getConsole().error("Failed to update node record for domain " + entry.getDomainName() + "!", throwable);
                        }).onSuccess(response -> domainAccess.createServiceRecords().onFailure(throwable
                                -> getApi().getConsole().error("Failed to create service records for domain " + entry.getDomainName() + "!", throwable)));
                        return;
                    }
                    domainAccess.createNodeRecord().onFailure(throwable -> {
                        getApi().getConsole().error("Failed to create node record for domain " + entry.getDomainName() + "!", throwable);
                    }).onSuccess(response -> domainAccess.createServiceRecords().onFailure(throwable
                            -> getApi().getConsole().error("Failed to create service records for domain " + entry.getDomainName() + "!", throwable)));
                });
        }

        CloudAPI.getInstance().getEventManager().register(new CloudServiceStartedListener());
        CloudAPI.getInstance().getEventManager().register(new CloudServiceStoppedListener());
    }

    @Override
    public void onDisable() {

        if(this.domainAccesses != null){
            for (CloudFlareDomainAccess domainAccess : this.domainAccesses) {

                if(domainAccess.getNodeRecordId() != null) {
                    domainAccess.deleteNodeRecord().onFailure(throwable -> {
                        getApi().getConsole().error("Failed to delete node record for domain " + domainAccess.getConfiguration().getDomainName() + "!", throwable);
                    });
                }

                for (ICloudService service : domainAccess.getServiceRecordIds().keySet()) {
                    domainAccess.deleteServiceRecord(service).onFailure(throwable -> {
                        getApi().getConsole().error("Failed to delete service record for domain " + domainAccess.getConfiguration().getDomainName() + "!", throwable);
                    }).onSuccess(response -> {
                        getApi().getConsole().info("Deleted service record for domain " + domainAccess.getConfiguration().getDomainName() + "!");
                    });
                }

                domainAccess.close();
            }

        }

        instance = null;
    }

    public String getNodePrefix(){
        return "rc-" + getApi().getNetworkComponentInfo().getIdentifier();
    }

}
