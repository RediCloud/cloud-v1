package net.suqatri.cloud.api.service.version;

import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.utils.Files;

import java.io.File;
import java.io.Serializable;

public interface ICloudServiceVersion extends Serializable {

    String getName();
    String getDownloadUrl();
    void setDownloadUrl(String url);
    ServiceEnvironment getEnvironmentType();
    void setEnvironmentType(ServiceEnvironment environmentType);
    boolean isPaperClip();
    void setPaperClip(boolean isPaperClip);
    default File getFile() {
        return new File(Files.VERSIONS_FOLDER.getFile(), getName() + ".jar");
    }
    File getPatchedFile();
    default boolean isPatched(){
        return this.getPatchedFile().exists();
    }
    default boolean isDownloaded(){
        return this.getFile().exists();
    }
    default boolean needPatch(){
        return this.isPaperClip() && !this.isPatched();
    }

}
