package net.suqatri.redicloud.node.file;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.file.IFileTransferManager;
import net.suqatri.redicloud.api.node.file.process.IFileTransferReceiveProcess;
import net.suqatri.redicloud.api.packet.PacketChannel;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.file.packet.*;
import net.suqatri.redicloud.node.file.process.FileTransferSendProcess;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
public class FileTransferManager implements IFileTransferManager {

    public static final int BYTES_PER_PACKET = 10485760;
    public static final int SLEEP_TIME_PER_PACKET = 60;
    private final ConcurrentHashMap<UUID, IFileTransferReceiveProcess> waitingReceiveProcesses;
    private FileTransferProcessThread thread;
    private FutureAction<Boolean> pullingRequest;

    public FileTransferManager() {
        this.waitingReceiveProcesses = new ConcurrentHashMap<>();
        this.thread = new FileTransferProcessThread();
        this.thread.start();
        this.registerPackets();
        CloudAPI.getInstance().getConsole().info("File transfer thread successfully started and file packets registered!");
    }

    private void registerPackets() {
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferStartPacket.class, PacketChannel.NODE);
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferBytesPacket.class, PacketChannel.NODE);
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferCompletedPacket.class, PacketChannel.NODE);
        CloudAPI.getInstance().getPacketManager().registerPacket(FileTransferCancelPacket.class, PacketChannel.NODE);
        CloudAPI.getInstance().getPacketManager().registerPacket(FileDeletePacket.class, PacketChannel.NODE);
        CloudAPI.getInstance().getPacketManager().registerPacket(FilePullPacket.class, PacketChannel.NODE);
    }

    public void addProcessQueue(UUID transferId) {
        if (!this.waitingReceiveProcesses.containsKey(transferId)) {
            CloudAPI.getInstance().getConsole().warn("File-read-transfer process with id " + transferId + " is not found or already processed!");
            return;
        }
        FileTransferProcessThread.getReceiveProcesses().put(transferId, this.waitingReceiveProcesses.get(transferId));
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, String targetFilePathToDelete, ICloudNode holder) {
        FutureAction<File> futureAction = new FutureAction<>();

        FileTransferSendProcess process = new FileTransferSendProcess();
        process.setOriginalFile(folder);
        process.setDestinationFilePath(targetFile.getPath());
        process.setTargetFilePathToDelete(targetFilePathToDelete);
        process.setReceiver(holder);
        process.setFutureAction(futureAction);

        futureAction.onFailure(t -> FileTransferProcessThread.getSentProcesses().remove(process.getTransferId()));

        FileTransferProcessThread.getSentProcesses().put(process.getTransferId(), process);

        return futureAction;
    }

    public FutureAction<File> transferFolderToNode(File folder, File targetFile, String targetFilePathToDelete, UUID nodeId) {
        FutureAction<File> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeId)
                .onFailure(futureAction)
                .onSuccess(node -> this.transferFolderToNode(folder, targetFile, targetFilePathToDelete, node)
                        .onFailure(futureAction)
                        .onSuccess(futureAction::complete));

        return futureAction;
    }

    public void cancelTransfer(UUID transferId, boolean interrupt) {
        if (FileTransferProcessThread.getCurrentReceiveProcess() != null) {
            if (FileTransferProcessThread.getCurrentReceiveProcess() != null) {
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
        if (FileTransferProcessThread.getCurrentSentProcess() != null) {
            if (FileTransferProcessThread.getCurrentSentProcess() != null) {
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

    public FutureAction<Boolean> pullFile(String originalFilePath, File destinationFile, File targetFileToDelete, ICloudNode holder) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        if (this.pullingRequest != null) {
            futureAction.completeExceptionally(new IllegalStateException("Pulling request is already in progress!"));
            return futureAction;
        }
        FilePullPacket packet = new FilePullPacket();
        packet.getPacketData().setChannel(PacketChannel.NODE);
        packet.setOriginalFilePath(originalFilePath);
        packet.setDestinationFilePath(destinationFile.getPath());
        packet.setTargetFilePathToDelete(targetFileToDelete.getPath());
        packet.getPacketData().addReceiver(holder.getNetworkComponentInfo());
        packet.publishAsync();

        this.pullingRequest = futureAction;
        this.pullingRequest.orTimeout(90, TimeUnit.SECONDS);
        this.pullingRequest.whenComplete((r, e) -> this.pullingRequest = null);

        return futureAction;
    }
}
