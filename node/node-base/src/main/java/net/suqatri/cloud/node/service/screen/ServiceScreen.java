package net.suqatri.cloud.node.service.screen;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.service.screen.IScreenLine;
import net.suqatri.cloud.api.node.service.screen.IServiceScreen;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.node.NodeLauncher;
import org.redisson.api.RList;
import org.redisson.codec.JsonJacksonCodec;

@Getter
public class ServiceScreen implements IServiceScreen {

    private static final int MAX_LINES = 50;
    private static final int CHECK_INTERVAL = 25;

    private final IRBucketHolder<ICloudService> service;
    private final RList<IScreenLine> lines;
    private int current = 0;

    public ServiceScreen(IRBucketHolder<ICloudService> service) {
        this.service = service;
        this.lines = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient().getList("screen@" + this.service.get().getUniqueId(), new JsonJacksonCodec());
    }

    @Override
    public void addLine(String line) {
        ScreenLine screenLine = new ScreenLine(this, line);
        this.lines.add(screenLine);

        this.current++;
        if(this.current > MAX_LINES) {
            removeUselessLines();
            this.current = 0;
        }

        ScreenLinePacket packet = new ScreenLinePacket();
        packet.setServiceId(this.service.get().getUniqueId());
        packet.setScreenLine(screenLine);
        packet.publishAllAsync(NetworkComponentType.NODE);

        if(NodeLauncher.getInstance().getConsole().getCurrentSetup() != null) return;
        if(!NodeLauncher.getInstance().getScreenManager().isActive(this)) return;
        screenLine.print();
    }

    @Override
    public void removeUselessLines(){
        CloudAPI.getInstance().getExecutorService().submit(() -> {
            if(this.lines.size() <= MAX_LINES) return;
            for(IScreenLine line : this.lines.readAll()){
                if(this.lines.size() <= MAX_LINES) break;
                this.lines.remove(line);
            }
        });
    }

    @Override
    public void delete() {
        this.lines.deleteAsync();
    }
}
