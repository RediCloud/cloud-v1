package net.suqatri.cloud.node.file.process;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.commons.file.FileUtils;
import net.suqatri.cloud.commons.file.Files;
import net.suqatri.cloud.commons.file.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

@Data
public class FileTransferReceiveProcess implements IFileTransferProcess {

    private INetworkComponentInfo senderNetworkComponentInfo;
    private final UUID transferId;
    private int indexes;
    private TreeMap<Integer, byte[]> receivedFileData = new TreeMap<>();

    private String originalFilePath;
    private String destinationFilePath;

    @Override
    public void process() {
        try {
            if(this.receivedFileData.size() != this.indexes){
                CloudAPI.getInstance().getConsole().error("File-read-transfer data size is not equal to indexes");
                return;
            }
            byte[] bytes = FileUtils.mergeByteArrays(new ArrayList<>(this.receivedFileData.values()));

            File zipFile = new File(Files.TEMP_FOLDER.getFile(), this.transferId + ".zip");
            FileUtils.bytesToFile(bytes, zipFile.getAbsolutePath());

            ZipUtils.unzipDir(zipFile, this.originalFilePath);
        } catch (IOException e) {
            CloudAPI.getInstance().getConsole().error("File-read-transfer process error", e);
        }
    }
}
