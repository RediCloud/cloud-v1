package net.suqatri.cloud.api.node.file.process;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.File;
import java.util.UUID;

public interface IFileTransferSendProcess extends IFileTransferProcess{

    UUID getTransferId();
    IRBucketHolder<ICloudNode> getReceiver();
    void setReceiver(IRBucketHolder<ICloudNode> receiver);
    void setOriginalFile(File originalFile);
    void setDestinationFilePath(String destinationFilePath);
    void setFutureAction(FutureAction<File> futureAction);
    void setZipFile(File zipFile);
    void setTargetFilePathToDelete(String targetFilePathToDelete);
    void setStartPacketSent(boolean startPacketSent);
    boolean isStartPacketSent();
    File getZipFile();
    String getTargetFilePathToDelete();
    String getDestinationFilePath();
    File getOriginalFile();
    FutureAction<File> getFutureAction();


}