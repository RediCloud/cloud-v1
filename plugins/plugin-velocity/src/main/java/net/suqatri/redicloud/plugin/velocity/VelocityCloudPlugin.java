package net.suqatri.redicloud.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

@Plugin(id = "redicloud-plugin", name = "RediCloud-Plugin", version = "1.0-SNAPSHOT", authors = "Suqatri")
public class VelocityCloudPlugin {

    private VelocityCloudAPI cloudAPI;

    @Inject
    public VelocityCloudPlugin(ProxyServer proxyServer, Logger logger) {
        //TODO fix this
        proxyServer.getScheduler().buildTask(this, () -> {
            cloudAPI = new VelocityCloudAPI(proxyServer, this);
        }).delay(2, TimeUnit.SECONDS).schedule();
    }

}
