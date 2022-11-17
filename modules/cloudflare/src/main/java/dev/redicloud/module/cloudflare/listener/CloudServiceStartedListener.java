package dev.redicloud.module.cloudflare.listener;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.service.event.CloudServiceStartedEvent;
import dev.redicloud.module.cloudflare.domain.CloudFlareDomainAccess;
import dev.redicloud.module.cloudflare.CloudFlareModule;
import dev.redicloud.module.cloudflare.configuration.CloudFlareGroupEntryConfiguration;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStarted(CloudServiceStartedEvent event){
        event.getServiceAsync()
            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get service", e))
            .onSuccess(service -> {
                for (CloudFlareDomainAccess domainAccess : CloudFlareModule.getInstance().getDomainAccesses()) {
                    for (CloudFlareGroupEntryConfiguration groupEntry : domainAccess.getConfiguration().getGroups()) {
                        if(groupEntry.getGroupName().equalsIgnoreCase(service.getGroupName())){
                            domainAccess.createServiceRecord(service, groupEntry);
                        }
                    }
                }
            });
    }

}
