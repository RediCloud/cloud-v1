package net.suqatri.cloud.node.file;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;
import net.suqatri.cloud.node.file.process.FileTransferSentProcess;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class FileTransferProcessThread extends Thread {

    private final ConcurrentHashMap<UUID, FileTransferSentProcess> sentProcesses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, FileTransferReceiveProcess> receiveProcesses = new ConcurrentHashMap<>();

    private FileTransferSentProcess currentSentProcess;
    private FileTransferReceiveProcess currentReceiveProcess;

    @Override
    public void run() {
        try {

            if(!this.sentProcesses.isEmpty()){
                this.currentSentProcess = this.sentProcesses.remove(0);
                CloudAPI.getInstance().getConsole().debug("Starting file-sent-transfer process for transfer id: " + this.currentSentProcess.getTransferId());
                this.currentSentProcess.process();
                this.currentSentProcess = null;
            }

            Thread.sleep(500);

            if(!this.receiveProcesses.isEmpty()){
                this.currentReceiveProcess = this.receiveProcesses.remove(0);
                CloudAPI.getInstance().getConsole().debug("Starting file-read-transfer process for transfer id: " + this.currentReceiveProcess.getTransferId());
                this.currentReceiveProcess.process();
                this.currentReceiveProcess = null;
            }

            Thread.sleep(500);
        } catch (Exception e) {
            if(this.currentSentProcess != null){
                this.currentSentProcess.getFutureAction().completeExceptionally(e);
                this.currentSentProcess = null;
            }
            if(this.currentReceiveProcess != null){
                CloudAPI.getInstance().getConsole().error("Error while processing file-read-transfer process for transfer id: " + this.currentReceiveProcess.getTransferId());
                this.currentReceiveProcess = null;
            }
        }
    }

}
