package net.suqatri.redicloud.node.service.factory;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.node.service.factory.ICloudPortManager;
import net.suqatri.redicloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CloudNodePortManager implements ICloudPortManager {

    private final ConcurrentHashMap<ICloudServiceProcess, Integer> usedPort = new ConcurrentHashMap<>();
    private final List<Integer> blockedPorts = new ArrayList<>();

    public FutureAction<Integer> getUnusedPort(ICloudServiceProcess process) {
        FutureAction<Integer> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getExecutorService().submit(() -> {
            int startPort = process.getServiceHolder().get().getConfiguration().getStartPort();
            boolean inRange = process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.MINECRAFT
                    ? (startPort >= 49152 && startPort <= 65535)
                    : (startPort >= 25500 && startPort <= 25600);
            int currentPort = !inRange ? ((process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.BUNGEECORD
                    || process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.VELOCITY)
                    ? 25565 : 49152)
                    : process.getServiceHolder().get().getConfiguration().getStartPort();
            if (!inRange) {
                if (process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.MINECRAFT) {
                    CloudAPI.getInstance().getConsole().warn("Service " + process.getServiceHolder().get().getServiceName()
                            + " has invalid start port " + process.getServiceHolder().get().getConfiguration().getStartPort()
                            + " (must be in range 49152-65535)");
                    CloudAPI.getInstance().getConsole().warn("Using default start port 49152");
                } else {
                    CloudAPI.getInstance().getConsole().warn("Service " + process.getServiceHolder().get().getServiceName()
                            + " has invalid start port " + process.getServiceHolder().get().getConfiguration().getStartPort()
                            + " (must be in range 25500-25600)");
                    CloudAPI.getInstance().getConsole().warn("Using default start port 25565");
                }
            }
            while (isInUse(currentPort) || isPortBlocked(currentPort)) {
                currentPort++;
                if (process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.MINECRAFT
                        ? currentPort >= 65535
                        : currentPort >= 25565
                ) currentPort = process.getServiceHolder().get().getEnvironment() == ServiceEnvironment.MINECRAFT
                        ? 49152
                        : 25500;
            }
            this.addBlockedPort(currentPort);
            process.setPort(currentPort);
            this.usedPort.put(process, currentPort);
            futureAction.complete(currentPort);
        });

        return futureAction;
    }

    public boolean isPortBlocked(int port) {
        if (this.blockedPorts.contains(port)) return true;
        return !isPortAvailable(port);
    }

    public boolean isPortAvailable(int port) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public boolean isInUse(int port) {
        return this.usedPort.containsKey(port);
    }

    public void unUsePort(ICloudServiceProcess process) {
        this.usedPort.remove(process);
        this.blockedPorts.removeIf(port -> port == process.getPort());
    }

    public void addBlockedPort(int port) {
        this.blockedPorts.add(port);
    }
}
