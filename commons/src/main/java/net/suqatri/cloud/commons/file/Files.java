package net.suqatri.cloud.commons.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor @Getter
public enum Files {

    CLOUD_FOLDER("./"),
    MODULES_FOLDER("modules"),
    STORAGE_FOLDER("storage"),
    TEMPLATE_FOLDER("templates"),
    TEMP_FOLDER("temp"),
    TEMP_SERVICE_FOLDER("temp/services"),
    LIBS_FOLDER("storage/libs"),
    LIBS_BLACKLIST_FOLDER("storage/libs/blacklist"),
    LIBS_REPO_FOLDER("storage/libs/repo"),
    LIBS_INFO_FOLDER("storage/libs/info"),
    RUNNER_JAR("runner.jar"),
    NODE_JAR("storage/node.jar"),
    MINECRAFT_PLUGIN_JAR("storage/plugin-minecraft.jar"),
    PROXY_PLUGIN_JAR("storage/plugin-proxy.jar"),
    NODE_JSON("node.json"),
    VERSIONS_FOLDER("storage/versions"),
    REDIS_CONFIG("storage/redis.json");

    private final String path;

    public File getFile(){
        return new File(path);
    }

    public void createIfNotExists(){
        if(!getFile().exists()){
            getFile().mkdirs();
        }
    }

    public boolean exists(){
        return getFile().exists();
    }

    public void downloadFromUrl(String url){
        //TODO
    }

}
