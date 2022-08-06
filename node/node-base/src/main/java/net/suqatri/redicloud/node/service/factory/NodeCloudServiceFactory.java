package net.suqatri.redicloud.node.service.factory;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.redicloud.api.impl.service.packet.stop.CloudServiceInitStopPacket;
import net.suqatri.redicloud.api.node.file.event.FilePulledTemplatesEvent;
import net.suqatri.redicloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.redicloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.service.NodeCloudServiceManager;

import java.util.UUID;

@Getter
@Setter
public class NodeCloudServiceFactory extends CloudServiceFactory implements ICloudNodeServiceFactory {

    private final NodeCloudServiceManager serviceManager;
    private final CloudNodePortManager portManager;
    private final CloudNodeServiceThread thread;

    public NodeCloudServiceFactory(NodeCloudServiceManager serviceManager) {
        super(serviceManager);
        this.serviceManager = serviceManager;
        this.portManager = new CloudNodePortManager();
        this.thread = new CloudNodeServiceThread(this);
        if (NodeLauncher.getInstance().isFirstTemplatePulled()) {
            this.thread.start();
        } else {
            CloudAPI.getInstance().getEventManager().register(FilePulledTemplatesEvent.class, event -> this.thread.start());
        }
    }

    @Override
    public FutureAction<ICloudService> queueService(IServiceStartConfiguration configuration) {
        this.thread.getQueue().add(configuration);
        configuration.listenToStart();
        configuration.getStartListener()
                .onSuccess(holder -> this.serviceManager.putInFetcher(holder.getServiceName(), holder.getUniqueId()));
        return configuration.getStartListener();
    }

    @Override
    public FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getServiceManager().getServiceAsync(uniqueId)
                .onFailure(futureAction)
                .onSuccess(serviceHolder -> {
                    if(serviceHolder.isExternal()){
                        CloudServiceInitStopPacket packet = new CloudServiceInitStopPacket();
                        packet.getPacketData().addReceiver(serviceHolder.getNetworkComponentInfo());
                        packet.publishAsync();

                        NodeLauncher.getInstance().getNode().setMemoryUsage(NodeLauncher.getInstance().getNode().getMemoryUsage()
                                - serviceHolder.getConfiguration().getMaxMemory());
                        NodeLauncher.getInstance().getNode().updateAsync();

                        CloudAPI.getInstance().getServiceManager().removeFromFetcher(serviceHolder.getServiceName());

                        CloudAPI.getInstance().getEventManager().postGlobalAsync(new CloudServiceStoppedEvent(serviceHolder));
                        return;
                    }
                    if (!serviceHolder.getNodeId().equals(NodeLauncher.getInstance().getNode().getUniqueId())) {
                        super.destroyServiceAsync(uniqueId, force)
                                .onFailure(futureAction)
                                .onSuccess(futureAction::complete);
                        return;
                    }
                    ICloudServiceProcess process = this.thread.getProcesses().get(uniqueId);
                    if (process == null) {
                        CloudAPI.getInstance().getConsole().debug("Service " + uniqueId + " was removed from queue.");
                        this.thread.getWaitForRemove().add(uniqueId);
                        futureAction.complete(true);
                        return;
                    }

                    CloudAPI.getInstance().getConsole().debug("Service " + uniqueId + " is being stopped.");
                    process.stopAsync(force)
                            .onFailure(futureAction)
                            .onSuccess(b -> {
                                if (process.getService().isStatic()) {
                                    process.getService().setServiceState(ServiceState.OFFLINE);
                                    process.getService().updateAsync()
                                            .onFailure(futureAction)
                                            .onSuccess(v -> futureAction.complete(true));
                                } else {
                                    this.serviceManager.removeFromFetcher(process.getService().getServiceName());
                                    this.serviceManager.deleteBucketAsync(process.getService().getUniqueId().toString())
                                            .onFailure(futureAction)
                                            .onSuccess(v -> futureAction.complete(true));
                                }
                            });
                });

        return futureAction;
    }

}
