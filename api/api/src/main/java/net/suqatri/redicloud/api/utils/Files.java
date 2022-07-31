package net.suqatri.redicloud.api.utils;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.commons.file.FileUtils;

import java.io.File;
import java.io.IOException;

@Getter
public enum Files {

    CLOUD_FOLDER("./"),
    MODULES_FOLDER("modules"),
    STORAGE_FOLDER("storage"),
    TEMPLATE_FOLDER("templates"),
    TEMP_FOLDER("temp"),
    TEMP_TRANSFER_FOLDER("temp/transfer"),
    TEMP_SERVICE_FOLDER("temp/services"),
    STATIC_SERVICE_FOLDER("static"),
    TEMP_VERSION_FOLDER("temp/versions"),
    LIBS_FOLDER("storage/libs"),
    LIBS_BLACKLIST_FOLDER("storage/libs/blacklist"),
    LIBS_REPO_FOLDER("storage/libs/repo"),
    LIBS_INFO_FOLDER("storage/libs/info"),
    RUNNER_JAR("runner.jar"),
    NODE_JAR("storage/node-base-%version%.jar"),
    MINECRAFT_PLUGIN_JAR("storage/plugin-minecraft-%version%.jar"),
    PROXY_PLUGIN_JAR("storage/plugin-proxy-%version%.jar"),
    NODE_JSON("node.json"),
    VERSIONS_FOLDER("storage/versions"),
    SERVER_ICON("storage/server-icon.png"),
    REDIS_CONFIG("storage/redis.json");

    private final String path;

    Files(String path) {
        this.path = path.replaceAll("%version%", CloudAPI.getVersion());
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
