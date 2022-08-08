package net.suqatri.redicloud.api.node.poll.timeout;

import lombok.Data;
import net.suqatri.redicloud.api.impl.configuration.Configuration;
import net.suqatri.redicloud.api.node.NodeCloudDefaultAPI;

import java.util.concurrent.TimeUnit;

@Data
public class TimeOutPoolConfiguration extends Configuration {

    private String identifier = "node-timeout-poll";
    private boolean enabled = true;
    private long packetResponseTimeout = 5000;
    private long nodeTimeOut = TimeUnit.MINUTES.toMillis(5);

}
