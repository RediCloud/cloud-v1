package net.suqatri.redicloud.api.impl.service.version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.io.IOException;

@Setter
@Getter
public class CloudServiceVersion extends RBucketObject implements ICloudServiceVersion {

    private String name;
    private String javaCommand = "java";
    private String downloadUrl;
    private ServiceEnvironment environmentType;
    private boolean paperClip;
    private boolean isDefaultVersion = false;

    @Override
    public File getFile(boolean forceGetExistFile) throws IOException, InterruptedException {
        if (forceGetExistFile && !isDownloaded()) download();
        return this.getFile();
    }

    @Override
    public File getFile() {
        return new File(Files.VERSIONS_FOLDER.getFile(), getName() + ".jar");
    }

    @Override
    public FutureAction<File> getFileAsync(boolean forceGetExistFile) {
        FutureAction<File> futureAction = new FutureAction<>();
        if (forceGetExistFile && !isDownloaded()) {
            downloadAsync()
                    .onFailure(futureAction)
                    .onSuccess(b -> futureAction.complete(getFile()));
        } else {
            futureAction.complete(getFile());
        }
        return futureAction;
    }

    @Override
    public File getPatchedFile(boolean forceGetExistFile) throws InterruptedException, IOException {
        if (forceGetExistFile) {
            if (!isDownloaded()) this.download();
            if (!needPatch()) return getFile(true);
            if (!isPatched()) this.patch();
        }
        return this.getPatchedFile();
    }

    @Override
    public File getPatchedFile() {
        if (!isPaperClip()) return getFile();
        return new File(Files.VERSIONS_FOLDER.getFile(), getName() + ".patched.jar");
    }

    @Override
    public FutureAction<File> getPatchedFileAsync(boolean forceGetExistFile) {
        FutureAction<File> futureAction = new FutureAction<>();

        if (forceGetExistFile && !isPatched()) {
            patchAsync()
                    .onFailure(futureAction)
                    .onSuccess(b -> futureAction.complete(getPatchedFile()));
        } else {
            futureAction.complete(getPatchedFile());
        }

        return futureAction;
    }

    @Override
    @JsonIgnore
    public boolean isPatched() {
        if (!isPaperClip()) return false;
        return getPatchedFile().exists();
    }

    @Override
    @JsonIgnore
    public boolean isDownloaded() {
        return getFile().exists();
    }

    @Override
    public boolean needPatch() {
        return !isPatched() && isPaperClip();
    }

    public boolean isPaperClip() {
        return this.paperClip;
    }

    public void patch() throws IOException, InterruptedException {
        CloudAPI.getInstance().getServiceVersionManager().patch(this, true);
    }

    public FutureAction<Boolean> patchAsync() {
        return CloudAPI.getInstance().getServiceVersionManager().patchAsync(this, true);
    }

    public void download() throws InterruptedException, IOException {
        CloudAPI.getInstance().getServiceVersionManager().download(this, true);
    }

    public FutureAction<Boolean> downloadAsync() {
        return CloudAPI.getInstance().getServiceVersionManager().downloadAsync(this, true);
    }

    @Override
    public String getIdentifier() {
        return this.name;
    }
}
