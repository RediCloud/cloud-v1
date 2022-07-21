package net.suqatri.cloud.api.service.version;

import net.suqatri.cloud.api.redis.bucket.IRBucketObject;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.File;
import java.io.IOException;

public interface ICloudServiceVersion extends IRBucketObject {

    String getName();

    String getDownloadUrl();
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


}
