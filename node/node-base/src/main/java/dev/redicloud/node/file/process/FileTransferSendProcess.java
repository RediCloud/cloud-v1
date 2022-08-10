package dev.redicloud.node.file.process;

import dev.redicloud.node.file.packet.FileTransferStartPacket;
import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.node.file.process.IFileTransferSendProcess;
import dev.redicloud.api.utils.Files;
import dev.redicloud.commons.file.FileUtils;
import dev.redicloud.commons.file.ZipUtils;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.node.file.FileTransferManager;
import dev.redicloud.node.file.packet.FileTransferBytesPacket;
import dev.redicloud.node.file.packet.FileTransferCancelPacket;
import dev.redicloud.node.file.packet.FileTransferCompletedPacket;

import java.io.File;
import java.util.UUID;

@Data
public class FileTransferSendProcess implements IFileTransferSendProcess {

    private final UUID transferId = UUID.randomUUID();
    private ICloudNode receiver;

    private File originalFile;
    private String destinationFilePath;

    private FutureAction<File> futureAction;
    private File zipFile;
    private String targetFilePathToDelete;

    private boolean startPacketSent = false;


    @Override
    public void process() {
        try {

            if (this.receiver.getNetworkComponentInfo().getType() == NetworkComponentType.SERVICE) {
                this.futureAction.completeExceptionally(new IllegalArgumentException("Cannot send file to service!"));
                return;
            }

            this.zipFile = new File(Files.TEMP_TRANSFER_FOLDER.getFile(), this.transferId + ".zip");

            ZipUtils.zipDir(this.originalFile, this.zipFile);

            futureAction.onFailure(e -> this.cancel());

            byte[] sentData = FileUtils.fileToBytes(this.zipFile.getAbsolutePath());
            int size = sentData.length;
            int packetCount = size / FileTransferManager.BYTES_PER_PACKET;
            if (size % FileTransferManager.BYTES_PER_PACKET != 0) {
                packetCount++;
            }
            FileTransferStartPacket startPacket = new FileTransferStartPacket();
            startPacket.setOriginalFilePath(this.originalFile.getPath());
            startPacket.setTargetFilePathToDelete(this.targetFilePathToDelete);
            startPacket.setDestinationFilePath(this.destinationFilePath);
            startPacket.setTransferId(this.transferId);
            startPacket.setIndexes(packetCount);
            startPacket.getPacketData().addReceiver(this.receiver.getNetworkComponentInfo());
            startPacket.publishAsync();
            this.startPacketSent = true;

            int index = 0;
            int count = 0;
            int packetSize = FileTransferManager.BYTES_PER_PACKET;
            while (count < sentData.length) {
                Thread.sleep(FileTransferManager.SLEEP_TIME_PER_PACKET);
                byte[] data;
                if (sentData.length - count < packetSize) {
                    data = new byte[sentData.length - count];
                    System.arraycopy(sentData, count, data, 0, sentData.length - count);
                } else {
                    data = new byte[packetSize];
                    System.arraycopy(sentData, count, data, 0, packetSize);
                }
                if (!this.receiver.isConnected()) {
                    cancel();
                    return;
                }
                FileTransferBytesPacket packet = new FileTransferBytesPacket();
                packet.setTransferId(this.transferId);
                packet.setIndexId(count);
                packet.setFileData(data);
                packet.getPacketData().addReceiver(this.receiver.getNetworkComponentInfo());
                packet.publishAsync();
                CloudAPI.getInstance().getConsole().debug("Send FileTransferBytesPacket with index " + index + " and transferId: " + this.transferId + " (Byte-Size: " + data.length + ")");
                count += packetSize;
                index++;
            }

            Thread.sleep(1500);

            FileTransferCompletedPacket completedPacket = new FileTransferCompletedPacket();
            completedPacket.setTransferId(this.transferId);
            completedPacket.getPacketData().addReceiver(this.receiver.getNetworkComponentInfo());
            completedPacket.publishAsync();

            this.zipFile.delete();

            this.futureAction.complete(this.originalFile);
        } catch (Exception e) {
            this.futureAction.completeExceptionally(e);
        }
    }

    @Override
    public void cancel() {
        if (this.zipFile != null) {
            if (this.zipFile.exists()) this.zipFile.delete();
        }
        if (this.futureAction.isDone()) return;
        if (!this.startPacketSent) return;
        FileTransferCancelPacket cancelPacket = new FileTransferCancelPacket();
        cancelPacket.setTransferId(this.transferId);
        cancelPacket.getPacketData().addReceiver(this.receiver.getNetworkComponentInfo());
        cancelPacket.publishAsync();
    }

    public static enum FileTransferSentProcessState {
        STARTED_ZIPPING,
        STARTED_SENDING,
        COMPLETED,
        CANCELED
    }
}
