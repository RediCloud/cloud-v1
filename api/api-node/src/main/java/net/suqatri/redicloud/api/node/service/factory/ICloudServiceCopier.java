package net.suqatri.redicloud.api.node.service.factory;

import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.io.IOException;

public interface ICloudServiceCopier {

    FutureAction<File> copyFilesAsync();

    File copyFiles() throws IOException, Exception;

    ICloudService getServices();

    File getServiceDirectory();

}
