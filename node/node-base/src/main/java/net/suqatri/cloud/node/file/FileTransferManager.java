package net.suqatri.cloud.node.file;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.file.process.FileTransferSentProcess;
import org.redisson.api.RTopic;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class FileTransferManager {

    private int bytesPerChunk = 104857600;
    private int sleepTimePerPacket = 500;
    private FileTransferProcessThread thread;

    public FileTransferManager() {
        this.thread = new FileTransferProcessThread();
        this.thread.start();
        CloudAPI.getInstance().getConsole().info("File transfer thread started successfully!");
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, INetworkComponentInfo componentInfo){
        FutureAction<File> futureAction = new FutureAction<>();

        futureAction.orTimeout(15, TimeUnit.SECONDS);

        FileTransferSentProcess process = new FileTransferSentProcess();
        process.setOriginalFile(folder);
        process.setDestinationFilePath(targetFile.getPath());
        process.setReceiver(componentInfo);
        process.setFutureAction(futureAction);
        process.setFutureAction(futureAction);

        this.thread.getProcesses().put(process.getTransferId(), process);

        return futureAction;
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, ICloudNode node){
        return this.transferFolderToNode(folder, targetFile, node.getNetworkComponentInfo());
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, UUID nodeId){
        return this.transferFolderToNode(folder, targetFile, CloudAPI.getInstance().getNetworkComponentManager().getComponentInfo(NetworkComponentType.NODE, nodeId.toString()));
    }

}
