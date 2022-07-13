package net.suqatri.cloud.node.file.packet;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;

import java.util.concurrent.TimeUnit;

public class FileTransferCompletedPacket extends FileTransferPacket {

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        NodeLauncher.getInstance().getFileTransferManager().addProcessQueue(this.getTransferId());
    }
}
