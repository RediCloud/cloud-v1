package dev.redicloud.api.service.version;

import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.io.IOException;

public interface ICloudServiceVersion extends IRBucketObject {

    String getName();

    String getJavaCommand();

    String getDownloadUrl();

    boolean isDefaultVersion();

    void setDownloadUrl(String url);

    ServiceEnvironment getEnvironmentType();

    void setEnvironmentType(ServiceEnvironment environmentType);

    boolean isPaperClip();

    void setPaperClip(boolean isPaperClip);

    File getFile(boolean forceGetExistFile) throws IOException, InterruptedException;

    FutureAction<File> getFileAsync(boolean forceGetExistFile);

    File getFile();

    File getPatchedFile(boolean forceGetExistFile) throws InterruptedException, IOException;

    FutureAction<File> getPatchedFileAsync(boolean forceGetExistFile);

    File getPatchedFile();

    boolean isPatched();

    boolean isDownloaded();

    boolean needPatch();

    void setJavaCommand(String command);

}
