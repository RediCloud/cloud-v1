package dev.redicloud.api.node.service.screen;

import dev.redicloud.api.service.ICloudService;
import org.redisson.api.RList;

public interface IServiceScreen {

    ICloudService getService();

    RList<IScreenLine> getLines();

    void addLine(String line);

    void removeUselessLines();

    void deleteLines();
}
