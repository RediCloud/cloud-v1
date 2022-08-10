package dev.redicloud.api.node.service.factory;

import dev.redicloud.commons.function.future.FutureAction;

public interface ICloudPortManager {

    FutureAction<Integer> getUnusedPort(ICloudServiceProcess process);

    boolean isPortBlocked(int port);

    boolean isPortAvailable(int port);

    boolean isInUse(int port);

    void unUsePort(ICloudServiceProcess process);

    void addBlockedPort(int port);

}
