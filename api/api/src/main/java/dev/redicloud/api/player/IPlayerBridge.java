package dev.redicloud.api.player;

import dev.redicloud.api.service.ICloudService;

import java.util.UUID;

public interface IPlayerBridge {

    void connect(ICloudService service);

    void connect(UUID serviceId);

    void sendMessage(String message);

    void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut);

    void sendActionbar(String message);

    void sendTab(String header, String footer);

    void connect(String server);

    void disconnect(String reason);

    boolean hasPermission(String permission);
}
