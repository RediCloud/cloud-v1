package dev.redicloud.module.cloudflare.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudFlareDomainEntryConfiguration {

    private boolean enabled = false;
    private String token = "";
    private String zoneId = "";
    private String domainName = "example.com";
    private List<CloudFlareGroupEntryConfiguration> groups = new ArrayList<>();

}
