package dev.redicloud.module.cloudflare.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudFlareGroupEntryConfiguration {

    private boolean enabled = false;
    private String groupName;
    private String sub = "@";
    private int priority = 1;
    private int weight = 1;

}
