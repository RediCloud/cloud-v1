package dev.redicloud.api.node.service.screen;

import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface IServiceScreenManager {

    IServiceScreen getServiceScreen(ICloudService service);

    FutureAction<IServiceScreen> join(IServiceScreen serviceScreen);

    void leave(IServiceScreen serviceScreen);

    boolean isActive(IServiceScreen serviceScreen);

    boolean isActive(UUID serviceId);

    Collection<IServiceScreen> getActiveScreens();

    boolean isAnyScreenActive();

    void write(String command);

}
