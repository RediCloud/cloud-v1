package dev.redicloud.api.node.file;

import dev.redicloud.api.node.file.process.IFileTransferReceiveProcess;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface IFileTransferManager {

    ConcurrentHashMap<UUID, IFileTransferReceiveProcess> getWaitingReceiveProcesses();

    FutureAction<Boolean> getPullingRequest();

    void addProcessQueue(UUID transferId);

    FutureAction<File> transferFolderToNode(File folder, File targetFile, String targetFilePathToDelete, ICloudNode holder);

    FutureAction<File> transferFolderToNode(File folder, File targetFile, String targetFilePathToDelete, UUID nodeId);

    void cancelTransfer(UUID transferId, boolean interrupt);

    FutureAction<Boolean> pullFile(String originalFilePath, File destinationFile, File targetFileToDelete, ICloudNode holder);


}
