package net.suqatri.cloud.node.file;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.file.packet.FileTransferBytesPacket;
import net.suqatri.cloud.node.file.packet.FileTransferCompletedPacket;
import net.suqatri.cloud.node.file.packet.FileTransferStartPacket;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;
import net.suqatri.cloud.node.file.process.FileTransferSentProcess;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
public class FileTransferManager {

    private int bytesPerChunk = 104857600;
    private int sleepTimePerPacket = 500;
    private FileTransferProcessThread thread;
    private ConcurrentHashMap<UUID, FileTransferReceiveProcess> waitingReceiveProcesses;

    public FileTransferManager() {
        this.waitingReceiveProcesses = new ConcurrentHashMap<>();
        this.thread = new FileTransferProcessThread();
        this.thread.start();
        this.registerPackets();
        CloudAPI.getInstance().getConsole().info("File transfer thread successfully started and file packets registered!");
    }

    private void registerPackets(){
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferStartPacket.class);
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferBytesPacket.class);
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferCompletedPacket.class);
    }

    public void addProcessQueue(UUID transferId) {
        if(!this.waitingReceiveProcesses.containsKey(transferId)){
            CloudAPI.getInstance().getConsole().warn("File-read-transfer process with id " + transferId + " is not found or already processed!");
            return;
        }
        this.thread.getReceiveProcesses().put(transferId, this.waitingReceiveProcesses.get(transferId));
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

        this.thread.getSentProcesses().put(process.getTransferId(), process);

        return futureAction;
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, ICloudNode node){
        return this.transferFolderToNode(folder, targetFile, node.getNetworkComponentInfo());
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, UUID nodeId){
        return this.transferFolderToNode(folder, targetFile, CloudAPI.getInstance().getNetworkComponentManager().getComponentInfo(NetworkComponentType.NODE, nodeId.toString()));
    }

}
