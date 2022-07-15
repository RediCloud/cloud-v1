package net.suqatri.cloud.api.node.service.factory;

import net.suqatri.cloud.commons.function.future.FutureAction;

public interface ICloudPortManager {

    FutureAction<Integer> getUnusedPort(ICloudServiceProcess process);

    boolean isPortBlocked(int port);

    boolean isPortAvailable(int port);

    boolean isInUse(int port);

    void unusePort(int port);

    void addBlockedPort(int port);

}
