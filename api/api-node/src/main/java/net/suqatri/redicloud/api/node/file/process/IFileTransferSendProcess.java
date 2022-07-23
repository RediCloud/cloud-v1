package net.suqatri.redicloud.api.node.file.process;

import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.File;
import java.util.UUID;

public interface IFileTransferSendProcess extends IFileTransferProcess {

    UUID getTransferId();

    IRBucketHolder<ICloudNode> getReceiver();

    void setReceiver(IRBucketHolder<ICloudNode> receiver);

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
