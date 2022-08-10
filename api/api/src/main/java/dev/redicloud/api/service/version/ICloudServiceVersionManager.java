package dev.redicloud.api.service.version;

import dev.redicloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.Collection;

public interface ICloudServiceVersionManager {

    ICloudServiceVersion getServiceVersion(String name);

    FutureAction<ICloudServiceVersion> getServiceVersionAsync(String name);

    ICloudServiceVersion createServiceVersion(ICloudServiceVersion serviceVersion, boolean fullInstall) throws IOException, InterruptedException;

    FutureAction<ICloudServiceVersion> createServiceVersionAsync(ICloudServiceVersion version, boolean fullInstall);

    Collection<ICloudServiceVersion> getServiceVersions();

    FutureAction<Collection<ICloudServiceVersion>> getServiceVersionsAsync();

    boolean existsServiceVersion(String name);

    FutureAction<Boolean> existsServiceVersionAsync(String name);

    boolean deleteServiceVersion(String name);

    FutureAction<Boolean> deleteServiceVersionAsync(String name);

    boolean patch(ICloudServiceVersion serviceVersion, boolean force) throws IOException, InterruptedException;

    FutureAction<Boolean> patchAsync(ICloudServiceVersion serviceVersion, boolean force);

    boolean download(ICloudServiceVersion serviceVersion, boolean force) throws IOException, InterruptedException;

    FutureAction<Boolean> downloadAsync(ICloudServiceVersion serviceVersion, boolean force);

}
