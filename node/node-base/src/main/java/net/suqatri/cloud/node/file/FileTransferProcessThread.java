package net.suqatri.cloud.node.file;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.file.process.IFileTransferReceiveProcess;
import net.suqatri.cloud.api.node.file.process.IFileTransferSentProcess;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;
import net.suqatri.cloud.node.file.process.FileTransferSentProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FileTransferProcessThread extends Thread {

    @Getter
    private static final ConcurrentHashMap<UUID, IFileTransferSentProcess> sentProcesses = new ConcurrentHashMap<>();
    @Getter
    private static final ConcurrentHashMap<UUID, IFileTransferReceiveProcess> receiveProcesses = new ConcurrentHashMap<>();
    @Setter @Getter
    private static IFileTransferSentProcess currentSentProcess;
    @Setter @Getter
    private static IFileTransferReceiveProcess currentReceiveProcess;

    @Override
    public void run() {

            while(Thread.currentThread().isAlive()){

                try {

                    Optional<IFileTransferSentProcess> sentProcess = sentProcesses.values().stream().findFirst();
                    if(sentProcess.isPresent()){
                        currentSentProcess = sentProcess.get();
                        sentProcesses.remove(currentSentProcess.getTransferId());
                        CloudAPI.getInstance().getConsole().debug("Starting file-sent-transfer process for transfer id: " + currentSentProcess.getTransferId());
                        currentSentProcess.process();
                        currentSentProcess = null;
                        Thread.sleep(5000);
                    }

                    Thread.sleep(500);

                    Optional<IFileTransferReceiveProcess> receiveProcess = receiveProcesses.values().stream().findFirst();
                    if(receiveProcess.isPresent()){
                        currentReceiveProcess = receiveProcess.get();
                        receiveProcesses.remove(currentReceiveProcess.getTransferId());
                        CloudAPI.getInstance().getConsole().debug("Starting file-read-transfer process for transfer id: " + currentReceiveProcess.getTransferId());
                        currentReceiveProcess.process();
                        currentReceiveProcess = null;
                        Thread.sleep(5000);
                    }

                    List<UUID> timeoutList = new ArrayList<>();
                    for (UUID transferId : NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().keySet()) {
                        IFileTransferReceiveProcess process = NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().get(transferId);
                        if(process.getLastAction().get() < (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))){
                            process.cancel();
                            timeoutList.add(transferId);
                        }
                    }
                    for (UUID transferId : timeoutList) {
                        NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().remove(transferId);
                    }

                    Thread.sleep(500);

                } catch (Exception e) {
                    if(currentSentProcess != null){
                        currentSentProcess.getFutureAction().completeExceptionally(e);
                        currentSentProcess = null;
                    }
                    if(currentReceiveProcess != null){
                        CloudAPI.getInstance().getConsole().error("Error while processing file-read-transfer process for transfer id: " + currentReceiveProcess.getTransferId());
                        currentReceiveProcess = null;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
    }

}
