package net.suqatri.redicloud.api.impl.poll.timeout;

import lombok.Data;
import net.suqatri.redicloud.api.impl.configuration.Configuration;

import java.util.concurrent.TimeUnit;

@Data
public class TimeOutPoolConfiguration extends Configuration {

    private String identifier = "node-timeout-poll";
    private boolean enabled = true;
    private long packetResponseTimeout = 5000;
    private long nodeTimeOut = TimeUnit.MINUTES.toMillis(5);

    @Override
    public String getIdentifier() {
        return this.identifier;
    }
}
