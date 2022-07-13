package net.suqatri.cloud.node.file;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.file.packet.FileTransferBytesPacket;
import net.suqatri.cloud.node.file.packet.FileTransferCompletedPacket;
import net.suqatri.cloud.node.file.packet.FileTransferStartPacket;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;
import net.suqatri.cloud.node.file.process.FileTransferSentProcess;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class FileTransferManager {

    public static final int BYTES_PER_PACKET = 10485760;
    public static final int SLEEP_TIME_PER_PACKET = 60;

    private FileTransferProcessThread thread;
    private final ConcurrentHashMap<UUID, FileTransferReceiveProcess> waitingReceiveProcesses;

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
        FileTransferProcessThread.getReceiveProcesses().put(transferId, this.waitingReceiveProcesses.get(transferId));
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, String targetFilePathToDelete, IRBucketHolder<ICloudNode> holder){
        FutureAction<File> futureAction = new FutureAction<>();

        FileTransferSentProcess process = new FileTransferSentProcess();
        process.setOriginalFile(folder);
        process.setDestinationFilePath(targetFile.getPath());
        process.setTargetFilePathToDelete(targetFilePathToDelete);
        process.setReceiver(holder);
        process.setFutureAction(futureAction);

        futureAction.onFailure(t -> FileTransferProcessThread.getSentProcesses().remove(process.getTransferId()));

        FileTransferProcessThread.getSentProcesses().put(process.getTransferId(), process);

        return futureAction;
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, String targetFilePathToDelete, UUID nodeId){
        FutureAction<File> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeId)
                .onFailure(futureAction)
                .onSuccess(nodeHolder -> this.transferFolderToNode(folder, targetFile, targetFilePathToDelete, nodeHolder)
                        .onFailure(futureAction)
                        .onSuccess(futureAction::complete));

        return futureAction;
    }

    public void cancelTransfer(UUID transferId, boolean interrupt){
        if(FileTransferProcessThread.getCurrentReceiveProcess() != null){
            if(FileTransferProcessThread.getCurrentReceiveProcess() != null) {
                if (FileTransferProcessThread.getCurrentReceiveProcess().getTransferId().equals(transferId)) {
                    if (interrupt) {
                        this.thread.interrupt();
                        this.thread = new FileTransferProcessThread();
                        this.thread.start();
                    }
                    FileTransferProcessThread.getCurrentReceiveProcess().cancel();
                    FileTransferProcessThread.setCurrentReceiveProcess(null);
                }
            }
        }
        if(FileTransferProcessThread.getCurrentSentProcess() != null){
            if(FileTransferProcessThread.getCurrentSentProcess() != null) {
                if (FileTransferProcessThread.getCurrentSentProcess().getTransferId().equals(transferId)) {
                    if (interrupt) {
                        this.thread.interrupt();
                        this.thread = new FileTransferProcessThread();
                        this.thread.start();
                    }
                    FileTransferProcessThread.getCurrentSentProcess().cancel();
                    FileTransferProcessThread.setCurrentSentProcess(null);
                }
            }
        }
    }

}
