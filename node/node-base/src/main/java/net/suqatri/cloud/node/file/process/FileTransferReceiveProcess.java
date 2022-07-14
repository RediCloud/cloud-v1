package net.suqatri.cloud.node.file.process;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.commons.file.FileUtils;
import net.suqatri.cloud.commons.file.Files;
import net.suqatri.cloud.commons.file.ZipUtils;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.packet.FileTransferReadFailedPacket;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class FileTransferReceiveProcess implements IFileTransferProcess {

    private INetworkComponentInfo senderNetworkComponentInfo;
    private final UUID transferId;
    private int indexes;
    private TreeMap<Integer, byte[]> receivedFileData = new TreeMap<>();

    private String originalFilePath;
    private String destinationFilePath;
    private File zipFile;
    private String targetFilePathToDelete;

    private AtomicLong lastAction = new AtomicLong(System.currentTimeMillis());

    @Override
    public void process() {
        try {
            this.lastAction.set(System.currentTimeMillis());
            if(this.receivedFileData.size() != this.indexes){
                CloudAPI.getInstance().getConsole().error("§cFile-read-transfer data size is not equal to indexes for transfer " + this.transferId);
                FileTransferReadFailedPacket packet = new FileTransferReadFailedPacket();
                packet.setTransferId(this.transferId);
                packet.setIndexesReceived(this.receivedFileData.size());
                packet.setIndexesSent(this.indexes);
                packet.getPacketData().addReceiver(this.getSenderNetworkComponentInfo());
                packet.publishAsync();
                this.receivedFileData.clear();
                return;
            }

            if(this.targetFilePathToDelete != null){
                File file = new File(Files.CLOUD_FOLDER.getFile(), this.targetFilePathToDelete);
                if(file.exists()) {
                    if(file.isDirectory()){
                        org.apache.commons.io.FileUtils.deleteDirectory(file);
                        file.mkdirs();
                    }else{
                        file.delete();
                    }
                }
            }

            File destinationFile = new File(Files.CLOUD_FOLDER.getFile(), this.destinationFilePath);
            if(!destinationFile.exists()) destinationFile.mkdirs();

            byte[] bytes = FileUtils.mergeByteArrays(this.receivedFileData.values());

            this.zipFile = new File(Files.TEMP_FOLDER.getFile(), this.transferId + ".zip");
            FileUtils.bytesToFile(bytes, this.zipFile.getAbsolutePath());
            this.lastAction.set(System.currentTimeMillis());
            ZipUtils.unzipDir(zipFile, destinationFile.getAbsolutePath());
            this.lastAction.set(System.currentTimeMillis());

            zipFile.delete();

            CloudAPI.getInstance().getConsole().debug("File-read-transfer " + this.transferId + " completed");

            FutureAction<Boolean> futureAction = NodeLauncher.getInstance().getFileTransferManager().getPullingRequest();
            if(futureAction != null) futureAction.complete(true);
        } catch (IOException e) {
            CloudAPI.getInstance().getConsole().error("File-read-transfer process error", e);
            this.receivedFileData.clear();
        }
    }

    @Override
    public void cancel() {
        this.receivedFileData.clear();
        if(this.zipFile != null) {
            if(this.zipFile.exists()) this.zipFile.delete();
        }
    }

    public static enum FileTransferReceiveProcessStatus {
        WAITING_FOR_BYTE_PACKETS,
        STARTED_UNZIPPING,
        BYTE_WRITING,
        FILE_WRITING,
        COMPLETED,
        CANCELED
    }
}
