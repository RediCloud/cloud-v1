package net.suqatri.cloud.node.service.screen;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.screen.IScreenLine;
import net.suqatri.cloud.api.node.service.screen.IServiceScreen;
import net.suqatri.cloud.api.node.service.screen.IServiceScreenManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceScreenManager implements IServiceScreenManager {

    private final ConcurrentHashMap<UUID, IServiceScreen> screens;
    private final List<IServiceScreen> activeScreens;

    public ServiceScreenManager(){
        this.activeScreens = new ArrayList<>();
        this.screens = new ConcurrentHashMap<>();
        CloudAPI.getInstance().getPacketManager().registerPacket(ScreenLinePacket.class);
    }

    @Override
    public IServiceScreen getServiceScreen(IRBucketHolder<ICloudService> serviceHolder) {
        if(!this.screens.containsKey(serviceHolder.get().getUniqueId())) {
            this.screens.put(serviceHolder.get().getUniqueId(), new ServiceScreen(serviceHolder));
        }
        return this.screens.get(serviceHolder.get().getUniqueId());
    }

    @Override
    public FutureAction<IServiceScreen> join(IServiceScreen serviceScreen) {
        FutureAction<IServiceScreen> futureAction = new FutureAction<>();
        if(this.activeScreens.contains(serviceScreen)) {
            futureAction.complete(serviceScreen);
            return futureAction;
        }
        this.activeScreens.add(serviceScreen);
        serviceScreen.getLines().readAllAsync().whenComplete((lines, e) -> {
            System.out.println(lines.size());
            for (IScreenLine screenLine : lines) {
                System.out.println(screenLine.getLine());
                CloudAPI.getInstance().getConsole().log(screenLine.createConsoleLine());
            }

            futureAction.complete(serviceScreen);
        });
        return futureAction;
    }

    @Override
    public void leave(IServiceScreen serviceScreen) {
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
