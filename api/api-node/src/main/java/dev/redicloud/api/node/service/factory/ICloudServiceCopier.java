package dev.redicloud.api.node.service.factory;

import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.io.IOException;

public interface ICloudServiceCopier {

    FutureAction<File> copyFilesAsync();

    File copyFiles() throws IOException, Exception;

    ICloudService getServices();

    File getServiceDirectory();

}
