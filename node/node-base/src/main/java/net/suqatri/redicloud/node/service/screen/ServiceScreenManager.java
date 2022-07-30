package net.suqatri.redicloud.node.service.screen;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.node.service.screen.IScreenLine;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreenManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.service.screen.packet.ScreenDestroyPacket;
import net.suqatri.redicloud.node.service.screen.packet.ScreenLinePacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceScreenManager implements IServiceScreenManager {

    private final ConcurrentHashMap<UUID, IServiceScreen> screens;
    private final List<IServiceScreen> activeScreens;

    public ServiceScreenManager() {
        this.activeScreens = new ArrayList<>();
        this.screens = new ConcurrentHashMap<>();

        CloudAPI.getInstance().getPacketManager().registerPacket(ScreenLinePacket.class);
        CloudAPI.getInstance().getPacketManager().registerPacket(ScreenDestroyPacket.class);
    }

    @Override
    public IServiceScreen getServiceScreen(IRBucketHolder<ICloudService> serviceHolder) {
        if (!this.screens.containsKey(serviceHolder.get().getUniqueId())) {
            this.screens.put(serviceHolder.get().getUniqueId(), new ServiceScreen(serviceHolder));
        }
        return this.screens.get(serviceHolder.get().getUniqueId());
    }

    @Override
    public FutureAction<IServiceScreen> join(IServiceScreen serviceScreen) {
        FutureAction<IServiceScreen> futureAction = new FutureAction<>();

        serviceScreen.getService().get().getConsoleNodeListenerIds().add(NodeLauncher.getInstance().getNode().getUniqueId());
        serviceScreen.getService().get().updateAsync();

        if (this.activeScreens.contains(serviceScreen)) {
            futureAction.complete(serviceScreen);
            return futureAction;
        }
        if(this.activeScreens.size() >= 2){
            CloudAPI.getInstance().getConsole().warn("Its recommended to have at most 2 screens open at once because of the performance issues!");
        }
        this.activeScreens.add(serviceScreen);

        serviceScreen.getLines().readAllAsync().whenComplete((lines, e) -> {
            if (e != null) {
                futureAction.completeExceptionally(e);
                return;
            }

            for (IScreenLine screenLine : lines) {
                CloudAPI.getInstance().getConsole().log(screenLine.createConsoleLine());
            }

            futureAction.complete(serviceScreen);
        });

        return futureAction;
    }

    @Override
    public void leave(IServiceScreen serviceScreen) {

        serviceScreen.getService().get().getConsoleNodeListenerIds().remove(NodeLauncher.getInstance().getNode().getUniqueId());
        serviceScreen.getService().get().updateAsync();

        this.activeScreens.remove(serviceScreen);
    }

    @Override
    public boolean isActive(IServiceScreen serviceScreen) {
        return this.activeScreens.contains(serviceScreen);
    }

    @Override
    public boolean isActive(UUID serviceId) {
        return this.activeScreens.parallelStream().anyMatch(screen -> screen.getService().get().getUniqueId().equals(serviceId));
    }

    @Override
    public Collection<IServiceScreen> getActiveScreens() {
        return new ArrayList<>(this.activeScreens);
    }

    @Override
    public boolean isAnyScreenActive() {
        return !this.activeScreens.isEmpty();
    }

}
