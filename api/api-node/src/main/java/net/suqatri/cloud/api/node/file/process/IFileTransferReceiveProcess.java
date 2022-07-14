package net.suqatri.cloud.api.node.file.process;

import net.suqatri.cloud.api.network.INetworkComponentInfo;

import java.io.File;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public interface IFileTransferReceiveProcess extends IFileTransferProcess{

    INetworkComponentInfo getSenderNetworkComponentInfo();
    UUID getTransferId();
    int getIndexes();
    TreeMap<Integer, byte[]> getReceivedFileData();

    String getOriginalFilePath();
    String getDestinationFilePath();
    File getZipFile();
    String getTargetFilePathToDelete();

    AtomicLong getLastAction();

    void setSenderNetworkComponentInfo(INetworkComponentInfo senderNetworkComponentInfo);
    void setIndexes(int indexes);
    void setReceivedFileData(TreeMap<Integer, byte[]> receivedFileData);

    void setOriginalFilePath(String originalFilePath);
    void setDestinationFilePath(String destinationFilePath);
    void setZipFile(File zipFile);
    void setTargetFilePathToDelete(String targetFilePathToDelete);

    void setLastAction(AtomicLong lastAction);

}
