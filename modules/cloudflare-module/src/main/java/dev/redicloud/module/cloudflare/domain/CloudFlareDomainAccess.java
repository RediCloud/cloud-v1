package dev.redicloud.module.cloudflare.domain;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.commons.function.future.FutureActionCollection;
import dev.redicloud.module.cloudflare.CloudFlareModule;
import dev.redicloud.module.cloudflare.configuration.CloudFlareDomainEntryConfiguration;
import dev.redicloud.module.cloudflare.configuration.CloudFlareGroupEntryConfiguration;
import eu.roboflax.cloudflare.CloudflareAccess;
import eu.roboflax.cloudflare.CloudflareRequest;
import eu.roboflax.cloudflare.CloudflareResponse;
import eu.roboflax.cloudflare.constants.Category;
import eu.roboflax.cloudflare.objects.dns.DNSRecord;
import eu.roboflax.cloudflare.objects.spectrum.DNS;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/*
    CloudFlare api docs: https://api.cloudflare.com
 */

@RequiredArgsConstructor
@Getter
public class CloudFlareDomainAccess {

    private final CloudFlareDomainEntryConfiguration configuration;
    private final CloudFlareModule module;
    private CloudflareAccess access;

    private String nodeRecordId = null;
    private final ConcurrentHashMap<ICloudService, String> serviceRecordIds = new ConcurrentHashMap<>();

    public boolean isInitialized(){
        if(this.access == null) return false;
        return this.access.isThreadPoolInitialized();
    }

    public void init() {
        if(this.isInitialized()) return;
        this.access = new CloudflareAccess(this.configuration.getToken());
    }

    public void close(){
        if(!this.isInitialized()) return;
        this.access.close();
    }

    public FutureAction<DNSRecord> createNodeRecord(){
        FutureAction<DNSRecord> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getScheduler().runTaskAsync(() -> {
            CloudflareResponse<DNSRecord> response = new CloudflareRequest(Category.CREATE_DNS_RECORD, this.access)
                    .identifiers(this.configuration.getZoneId())
                    .body("{" +
                            "\"type\":\"A\"," +
                            "\"name\":\"" + this.module.getNodePrefix() + "\"," +
                            "\"content\":\"" + this.module.getApi().getNetworkComponentInfo().getAsNode().getHostname() + "\"," +
                            "\"ttl\":1," +
                            "\"proxied\":false" +
                            "}")
                    .asObject(DNSRecord.class);

            if(!response.isSuccessful()) {
                futureAction.completeExceptionally(new RuntimeException(response.getErrors().toString()));
                return;
            }

            this.nodeRecordId = response.getObject().getId();

            futureAction.complete(response.getObject());
        });

        return futureAction;
    }

    public FutureAction<DNSRecord> getNodeRecord(){
        FutureAction<DNSRecord> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getScheduler().runTaskAsync(() -> {

            CloudflareResponse<List<DNSRecord>> response = new CloudflareRequest(Category.LIST_DNS_RECORDS, this.access)
                    .identifiers(this.configuration.getZoneId())
                    .asObjectList(DNSRecord.class);

            if(!response.isSuccessful()){
                CloudAPI.getInstance().getConsole().error("Failed to get dns records for node " + this.module.getApi().getNetworkComponentInfo().getAsNode().getName() + "!");
                CloudAPI.getInstance().getConsole().error("Response: " + response.getJson());
                futureAction.completeExceptionally(new RuntimeException("Failed to get dns records for node " + this.module.getApi().getNetworkComponentInfo().getAsNode().getName() + "!"));
                return;
            }

            for(DNSRecord record : response.getObject()){
                if(record.getName().equalsIgnoreCase(this.module.getNodePrefix())){
                    futureAction.complete(record);
                    return;
                }
            }

            futureAction.completeExceptionally(new NullPointerException("No dns record found for node " + this.module.getApi().getNetworkComponentInfo().getAsNode().getName() + "!"));
        });

        return futureAction;
    }

    public FutureAction<Boolean> existsNodeRecord(){
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if(this.nodeRecordId != null){
            futureAction.complete(true);
            return futureAction;
        }

        getNodeRecord().onSuccess(record -> {
            futureAction.complete(record != null);
        }).onFailure(e -> {
            if(e instanceof NullPointerException){
                futureAction.complete(false);
                return;
            }
            futureAction.completeExceptionally(e);
        });

        return futureAction;
    }

    public FutureAction<DNSRecord> updateNodeRecord(){
        FutureAction<DNSRecord> futureAction = new FutureAction<>();

        this.module.getApi().getScheduler().runTaskAsync(() -> {
            CloudflareResponse<DNSRecord> response = new CloudflareRequest(Category.UPDATE_DNS_RECORD, this.access)
                    .identifiers(this.configuration.getZoneId(), this.nodeRecordId)
                    .body("{" +
                            "\"type\":\"A\"," +
                            "\"name\":\"" + this.module.getNodePrefix() + "\"," +
                            "\"content\":\"" + this.module.getApi().getNetworkComponentInfo().getAsNode().getHostname() + "\"," +
                            "\"ttl\":1," +
                            "\"proxied\":false" +
                            "}")
                    .asObject(DNSRecord.class);

            if(!response.isSuccessful()){
                futureAction.completeExceptionally(new RuntimeException(response.getErrors().toString()));
                return;
            }

            futureAction.complete(response.getObject());
        });

        return futureAction;
    }

    public FutureAction<Boolean> deleteNodeRecord(){
        FutureAction<Boolean> futureAction = new FutureAction<>();

        this.module.getApi().getScheduler().runTaskAsync(() -> {
            CloudflareResponse<Void> response = new CloudflareRequest(Category.DELETE_DNS_RECORD, this.access)
                    .identifiers(this.configuration.getZoneId(), this.nodeRecordId)
                    .send();

            if(!response.isSuccessful()){
                futureAction.completeExceptionally(new RuntimeException(response.getErrors().toString()));
                return;
            }

            futureAction.complete(true);
        });

        return futureAction;
    }


    public FutureAction<DNSRecord> createServiceRecord(ICloudService service, CloudFlareGroupEntryConfiguration groupConfiguration){
        FutureAction<DNSRecord> futureAction = new FutureAction<>();

        this.module.getApi().getScheduler().runTaskAsync(() -> {
            CloudflareResponse<DNSRecord> response = new CloudflareRequest(Category.CREATE_DNS_RECORD, this.access)
                    .identifiers(this.configuration.getZoneId())
                    .body("{" +
                            "\"type\":\"SRV\"," +
                            "\"ttl\":1," +
                            "\"proxied\":false" +
                            "\"data\":{" +
                                "\"service\":\"_minecraft\"," +
                                "\"proto\":\"_tcp\"," +
                                "\"name\":\"" + groupConfiguration.getSub() + "\"," +
                                "\"priority\":" + groupConfiguration.getPriority() + "," +
                                "\"weight\":" + groupConfiguration.getWeight() + "," +
                                "\"port\":" + service.getPort() + "," +
                                "\"target\":\"" + this.module.getApi().getNetworkComponentInfo().getAsNode().getHostname() + "\"" +
                                "}" +
                            "}")
                    .asObject(DNSRecord.class);

            if(!response.isSuccessful()){
                futureAction.completeExceptionally(new RuntimeException(response.getErrors().toString()));
                return;
            }

            this.serviceRecordIds.put(service, response.getObject().getId());

            futureAction.complete(response.getObject());
        });

        return futureAction;
    }

    public FutureAction<Boolean> deleteServiceRecord(ICloudService service){
        FutureAction<Boolean> futureAction = new FutureAction<>();

        if(!this.serviceRecordIds.containsKey(service)) {
            futureAction.complete(false);
            return futureAction;
        }

        this.module.getApi().getScheduler().runTaskAsync(() -> {
            CloudflareResponse<Void> response = new CloudflareRequest(Category.DELETE_DNS_RECORD, this.access)
                    .identifiers(this.configuration.getZoneId(), this.serviceRecordIds.get(service))
                    .send();

            if(!response.isSuccessful()){
                futureAction.completeExceptionally(new RuntimeException(response.getErrors().toString()));
                return;
            }

            this.serviceRecordIds.remove(service);

            futureAction.complete(true);
        });

        return futureAction;
    }

    public FutureAction<List<DNSRecord>> createServiceRecords(){
        FutureAction<List<DNSRecord>> futureAction = new FutureAction<>();

        this.module.getApi().getServiceManager().getServicesAsync()
            .onFailure(futureAction)
            .onSuccess(services -> {

                FutureActionCollection<String, DNSRecord> collection = new FutureActionCollection<>();

                for(ICloudService service : services){

                    if(this.serviceRecordIds.containsKey(service)) continue;

                    CloudFlareGroupEntryConfiguration groupConfiguration = this.configuration.getGroups().parallelStream()
                            .filter(group -> group.getGroupName().equalsIgnoreCase(service.getGroupName()))
                            .findFirst().orElse(null);

                    if(groupConfiguration == null) continue;

                    collection.addToProcess(createServiceRecord(service, groupConfiguration));
                }

                collection.process()
                        .onFailure(futureAction)
                        .onSuccess(map -> futureAction.complete(new ArrayList<>(map.values())));
            });

        return futureAction;
    }

}
