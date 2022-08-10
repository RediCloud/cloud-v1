package dev.redicloud.api.node.file.process;

import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.util.UUID;

public interface IFileTransferSendProcess extends IFileTransferProcess {

    UUID getTransferId();

    ICloudNode getReceiver();

    void setReceiver(ICloudNode receiver);

    boolean isStartPacketSent();

    void setStartPacketSent(boolean startPacketSent);

    File getZipFile();

    void setZipFile(File zipFile);

    String getTargetFilePathToDelete();

    void setTargetFilePathToDelete(String targetFilePathToDelete);

    String getDestinationFilePath();

    void setDestinationFilePath(String destinationFilePath);

    File getOriginalFile();

    void setOriginalFile(File originalFile);

    FutureAction<File> getFutureAction();

    void setFutureAction(FutureAction<File> futureAction);


}
