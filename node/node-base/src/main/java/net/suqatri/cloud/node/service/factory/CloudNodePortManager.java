package net.suqatri.cloud.node.service.factory;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.factory.ICloudPortManager;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CloudNodePortManager implements ICloudPortManager {

    //TODO remove on service stop
    private final ConcurrentHashMap<ICloudServiceProcess, Integer> usedPort = new ConcurrentHashMap<>();
    private final List<Integer> blockedPorts = new ArrayList<>();

    public FutureAction<Integer> getUnusedPort(ICloudServiceProcess process) {
        FutureAction<Integer> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getExecutorService().submit(() -> {
            int startPort = process.getServiceHolder().get().getConfiguration().getStartPort() == -1 ? 5200 : process.getServiceHolder().get().getConfiguration().getStartPort();
            int currentPort = startPort;
            while(isInUse(currentPort) || isPortBlocked(currentPort)) currentPort++;
            this.usedPort.put(process, currentPort);
            futureAction.complete(currentPort);
        });

        return futureAction;
    }

    public boolean isPortBlocked(int port) {
        if(this.blockedPorts.contains(port)) return true;
        return isPortAvailable(port);
    }

    public boolean isPortAvailable(int port){
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", port);
            socket.close();
            this.blockedPorts.add(port);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public boolean isInUse(int port){
        return this.usedPort.containsKey(port);
    }

    public void unusePort(int port){
        this.usedPort.remove(port);
    }

    public void addBlockedPort(int port){
        this.blockedPorts.add(port);
    }
}
