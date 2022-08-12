package dev.redicloud.node.service.screen;

import dev.redicloud.node.service.screen.packet.ScreenLinePacket;
import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.node.service.screen.IScreenLine;
import dev.redicloud.api.node.service.screen.IServiceScreen;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.node.NodeLauncher;
import org.redisson.api.RList;
import org.redisson.codec.JsonJacksonCodec;

import java.util.UUID;

@Getter
public class ServiceScreen implements IServiceScreen {

    private static final int MAX_LINES = 60;
    private static final int CHECK_INTERVAL = 25;

    private static final int MAX_LINES_PER_SECOND = 15;
    private static final int MAX_LINES_FAIL_UNTIL_BLOCK = 3;

    private final ICloudService service;
    private final RList<IScreenLine> lines;
    private int current = 0;

    public ServiceScreen(ICloudService service) {
        this.service = service;
        this.lines = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient()
                .getList("screen-log:" + this.service.getUniqueId(), new JsonJacksonCodec());
    }

    @Override
    public void addLine(String line) {
        ScreenLine screenLine = new ScreenLine(this.getService().getServiceName(), line);
        this.lines.add(screenLine);

        this.current++;
        if (this.current > MAX_LINES) {
            removeUselessLines();
            this.current = 0;
        }

        if (this.getService().getConsoleNodeListenerIds().isEmpty()) return;

        ScreenLinePacket packet = null;
        for (UUID nodeId : this.getService().getConsoleNodeListenerIds()) {
            if (nodeId.equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
            if (packet == null) {
                packet = new ScreenLinePacket();
                packet.getPacketData().setChannel(PacketChannel.NODE);
                packet.setServiceId(this.service.getUniqueId());
                packet.setScreenLine(screenLine);
            }
            packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager().getComponentInfo(NetworkComponentType.NODE, nodeId));
        }
        if (packet != null) packet.publishAsync();

        if (NodeLauncher.getInstance().getConsole().getCurrentSetup() != null) return;
        if (!NodeLauncher.getInstance().getScreenManager().isActive(this)) return;
        screenLine.print();
    }

    @Override
    public void removeUselessLines() {
        this.lines.sizeAsync()
                .whenComplete((size, throwable) -> {
                    if(throwable != null) {
                        CloudAPI.getInstance().getConsole().error("Error while getting screen log size", throwable);
                        return;
                    }
                    if (size <= MAX_LINES) return;
                    this.lines.readAllAsync().whenComplete((lines, throwable1) -> {
                        if(throwable1 != null){
                            CloudAPI.getInstance().getConsole().error("Error while getting screen log of service " + this.service.getServiceName(), throwable1);
                            return;
                        }
                        int cachedSize = size;
                        for (IScreenLine line : lines) {
                            if (cachedSize <= MAX_LINES) break;
                            this.lines.removeAsync(line);
                            cachedSize--;
                        }
                    });
                });
    }

    @Override
    public void deleteLines() {
        this.lines.deleteAsync();
    }
}
