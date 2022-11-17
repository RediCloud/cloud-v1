package dev.redicloud.module.cloudflare;

import dev.redicloud.module.cloudflare.configuration.CloudFlareDomainEntryConfiguration;
import eu.roboflax.cloudflare.CloudflareAccess;
import eu.roboflax.cloudflare.CloudflareRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CloudFlareDomainAccess {

    private final CloudFlareDomainEntryConfiguration configuration;
    private CloudflareAccess access;

    public boolean isInitialized(){
        if(this.access == null) return false;
        return this.access.isThreadPoolInitialized();
    }

    public void test(){
        new CloudflareRequest(Category.ZONE)
    }

    public void init() {
        if(this.isInitialized()) return;
        this.access = new CloudflareAccess(this.configuration.getToken());
    }

    public void close(){
        if(!this.isInitialized()) return;
        this.access.close();
    }

}
