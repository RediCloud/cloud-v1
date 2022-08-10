package dev.redicloud.api.node.file.process;

import dev.redicloud.api.network.INetworkComponentInfo;

import java.io.File;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public interface IFileTransferReceiveProcess extends IFileTransferProcess {

    INetworkComponentInfo getSenderNetworkComponentInfo();

    void setSenderNetworkComponentInfo(INetworkComponentInfo senderNetworkComponentInfo);

    UUID getTransferId();

    int getIndexes();

    void setIndexes(int indexes);

    TreeMap<Integer, byte[]> getReceivedFileData();

    void setReceivedFileData(TreeMap<Integer, byte[]> receivedFileData);

    String getOriginalFilePath();

    void setOriginalFilePath(String originalFilePath);

    String getDestinationFilePath();

    void setDestinationFilePath(String destinationFilePath);

    File getZipFile();

    void setZipFile(File zipFile);

    String getTargetFilePathToDelete();

    void setTargetFilePathToDelete(String targetFilePathToDelete);

    AtomicLong getLastAction();

    void setLastAction(AtomicLong lastAction);

}
