package net.suqatri.cloud.node.file.process;

import lombok.Data;

import java.util.HashMap;
import java.util.UUID;

@Data
public class FileTransferReceiveProcess {

    private UUID senderNodeId;
    private final UUID transferId;
    private int indexes;
    private HashMap<Integer, byte[]> receivedFileData;

    private String originalFilePath;
    private String destinationFilePath;

}
