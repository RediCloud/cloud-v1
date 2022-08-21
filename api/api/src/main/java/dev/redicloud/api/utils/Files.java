package dev.redicloud.api.utils;

import lombok.Getter;
import dev.redicloud.api.CloudAPI;

import java.io.File;

@Getter
public enum Files {

    CLOUD_FOLDER("./"),
    MODULES_FOLDER("modules"),
    STORAGE_FOLDER("storage"),
    TEMPLATE_FOLDER("templates"),
    TEMP_FOLDER("tmp"),
    TEMP_TRANSFER_FOLDER("tmp/transfer"),
    TEMP_SERVICE_FOLDER("tmp/services"),
    STATIC_SERVICE_FOLDER("static"),
    TEMP_VERSION_FOLDER("tmp/versions"),
    LIBS_FOLDER("storage/libs"),
    LIBS_BLACKLIST_FOLDER("storage/libs/blacklist"),
    LIBS_REPO_FOLDER("storage/libs/repo"),
    LIBS_INFO_FOLDER("storage/libs/info"),
    NODE_JAR("redicloud-node-base.jar"),
    DEPENDENCY_AGENT("storage/libs/dependency-agent.jar"),
    MINECRAFT_PLUGIN_JAR("storage/redicloud-plugin-minecraft.jar"),
    BUNGEECORD_PLUGIN_JAR("storage/redicloud-plugin-bungeecord.jar"),
    VELOCITY_PLUGIN_JAR("storage/redicloud-plugin-velocity.jar"),
    NODE_JSON("node.json"),
    VERSIONS_FOLDER("storage/versions"),
    SERVER_ICON("storage/server-icon.png"),
    LOG_FOLDER("storage/logs/"),
    LOG_FILE("storage/logs/redicloud-%time%.log"),
    REDIS_CONFIG("storage/redis.json");

    private final String path;

    Files(String path) {
        this.path = path.replaceAll("%version%", CloudAPI.getInstance().getProperties().getVersion());
    }

    public File getFile() {
        return new File(path);
    }

    public void createIfNotExists() {
        if (!getFile().exists()) {
            getFile().mkdirs();
        }
    }

    public boolean exists() {
        return getFile().exists();
    }

}
