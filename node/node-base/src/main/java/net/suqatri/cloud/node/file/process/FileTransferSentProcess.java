package net.suqatri.cloud.node.file.process;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.commons.file.FileUtils;
import net.suqatri.cloud.commons.file.Files;
import net.suqatri.cloud.commons.file.ZipUtils;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.packet.FileTransferBytesPacket;
import net.suqatri.cloud.node.file.packet.FileTransferCompletedPacket;
import net.suqatri.cloud.node.file.packet.FileTransferStartPacket;

import java.io.File;
import java.util.UUID;

@Data
public class FileTransferSentProcess implements IFileTransferProcess {

    private final UUID transferId = UUID.randomUUID();
    private INetworkComponentInfo receiver;

    private File originalFile;
    private String destinationFilePath;

    private FutureAction<File> futureAction;
    private File zipFile;

    @Override
    public void process() {
        try{

            if(this.receiver.getType() == NetworkComponentType.SERVICE){
                this.futureAction.completeExceptionally(new IllegalArgumentException("Cannot send file to service!"));
                return;
            }

            this.zipFile = new File(Files.TEMP_FOLDER.getFile(), this.transferId + ".zip");

            ZipUtils.zipDirFiles(this.originalFile, FileUtils.getAllFilesAndDirs(this.originalFile), this.zipFile);

            byte[] sentData = FileUtils.fileToBytes(this.zipFile.getAbsolutePath());
            int size = sentData.length;
            int packetCount = size / NodeLauncher.getInstance().getFileTransferManager().getBytesPerChunk();
            if(size % NodeLauncher.getInstance().getFileTransferManager().getBytesPerChunk() != 0){
                packetCount++;
            }
            FileTransferStartPacket startPacket = new FileTransferStartPacket();
            startPacket.setOriginalFilePath(this.originalFile.getPath());
            startPacket.setDestinationFilePath(this.destinationFilePath);
            startPacket.setTransferId(this.transferId);
            startPacket.setIndexes(packetCount);
            startPacket.getPacketData().addReceiver(this.receiver);
            startPacket.publish();

            int index = 0;
            int packetSize = NodeLauncher.getInstance().getFileTransferManager().getBytesPerChunk();
            while(index < sentData.length){
                byte[] data = new byte[packetSize];
                System.arraycopy(sentData, index, data, 0, packetSize);
                FileTransferBytesPacket packet = new FileTransferBytesPacket();
                packet.setTransferId(this.transferId);
                packet.setIndexId(index);
                packet.setFileData(data);
                packet.getPacketData().addReceiver(this.receiver);
                packet.publish();
                CloudAPI.getInstance().getConsole().debug("Send FileTransferBytesPacket with index " + index + " and transferId: " + this.transferId + " (Byte-Size: " + data.length + ")");
                Thread.sleep(NodeLauncher.getInstance().getFileTransferManager().getSleepTimePerPacket());
                index += packetSize;
            }

            Thread.sleep(NodeLauncher.getInstance().getFileTransferManager().getSleepTimePerPacket());
            FileTransferCompletedPacket completedPacket = new FileTransferCompletedPacket();
            completedPacket.setTransferId(this.transferId);
            completedPacket.getPacketData().addReceiver(this.receiver);
            completedPacket.publish();

            this.zipFile.delete();

            this.futureAction.complete(this.originalFile);
        }catch (Exception e){
            this.futureAction.completeExceptionally(e);
        }
    }
}
