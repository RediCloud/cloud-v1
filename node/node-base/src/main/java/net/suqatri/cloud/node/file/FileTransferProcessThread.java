package net.suqatri.cloud.node.file;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.node.file.packet.FileTransferBytesPacket;
import net.suqatri.cloud.node.file.packet.FileTransferCompletedPacket;
import net.suqatri.cloud.node.file.packet.FileTransferStartPacket;
import net.suqatri.cloud.node.file.process.FileTransferSentProcess;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class FileTransferProcessThread extends Thread {

    private final ConcurrentHashMap<UUID, FileTransferSentProcess> processes = new ConcurrentHashMap<>();

    private FileTransferSentProcess currentProcess;
    private boolean sentStartPacket = false;
    private int bytePacketsCount = 0;
    private boolean completedPacket = false;

    @Override
    public void run() {
        try {

            if(!this.processes.isEmpty()){
                this.currentProcess = this.processes.remove(0);
                CloudAPI.getInstance().getConsole().debug("Starting file transfer process for transfer id: " + this.currentProcess.getTransferId());
                this.currentProcess.process();
            }

            Thread.sleep(500);
        } catch (Exception e) {
            if(this.currentProcess != null){
                this.currentProcess.getFutureAction().completeExceptionally(e);
                this.currentProcess = null;
            }
        }
    }

}
