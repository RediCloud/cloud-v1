package net.suqatri.redicloud.node.service.screen;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.node.service.screen.IScreenLine;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.service.screen.packet.ScreenLinePacket;
import org.redisson.api.RList;
import org.redisson.codec.JsonJacksonCodec;

import java.util.UUID;

@Getter
public class ServiceScreen implements IServiceScreen {

    private static final int MAX_LINES = 60;
    private static final int CHECK_INTERVAL = 25;

    private static final int MAX_LINES_PER_SECOND = 15;
    private static final int MAX_LINES_FAIL_UNTIL_BLOCK = 3;

    private final IRBucketHolder<ICloudService> service;
    private final RList<IScreenLine> lines;
    private int current = 0;

    public ServiceScreen(IRBucketHolder<ICloudService> service) {
        this.service = service;
        this.lines = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient().getList("screen-log@" + this.service.get().getUniqueId(), new JsonJacksonCodec());
    }

    @Override
    public void addLine(String line) {
        ScreenLine screenLine = new ScreenLine(this.getService().get().getServiceName(), line);
        this.lines.add(screenLine);

        this.current++;
        if (this.current > MAX_LINES) {
            removeUselessLines();
            this.current = 0;
        }

        if (this.getService().get().getConsoleNodeListenerIds().isEmpty()) return;

        ScreenLinePacket packet = null;
        for (UUID nodeId : this.getService().get().getConsoleNodeListenerIds()) {
            if (nodeId.equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
            if (packet == null) {
                packet = new ScreenLinePacket();
                packet.setServiceId(this.service.get().getUniqueId());
                packet.setScreenLine(screenLine);
            }
            packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager().getComponentInfo(NetworkComponentType.NODE, nodeId.toString()));
        }
        if (packet != null) packet.publishAsync();

        if (NodeLauncher.getInstance().getConsole().getCurrentSetup() != null) return;
        if (!NodeLauncher.getInstance().getScreenManager().isActive(this)) return;
        screenLine.print();
    }

    @Override
    public void removeUselessLines() {
        this.lines.sizeAsync()
                .thenAccept(size -> {
                    if (size <= MAX_LINES) return;
                    this.lines.readAllAsync().thenAccept(lines -> {
                        for (IScreenLine line : lines) {
                            if (this.lines.size() <= MAX_LINES) break;
                            this.lines.removeAsync(line);
                        }
                    });
                });
    }

    @Override
    public void deleteLines() {
        this.lines.deleteAsync();
    }
}
