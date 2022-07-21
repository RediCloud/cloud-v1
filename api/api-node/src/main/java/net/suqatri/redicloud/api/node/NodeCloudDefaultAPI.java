package net.suqatri.redicloud.api.node;

import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.node.CloudNode;
import net.suqatri.redicloud.api.impl.poll.timeout.ITimeOutPollManager;
import net.suqatri.redicloud.api.node.file.IFileTransferManager;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreenManager;
import net.suqatri.redicloud.api.utils.ApplicationType;

public abstract class NodeCloudDefaultAPI extends CloudDefaultAPIImpl<CloudNode> {

    public NodeCloudDefaultAPI() {
        super(ApplicationType.NODE);
    }

    public abstract CloudNode getNode();
    public abstract IFileTransferManager getFileTransferManager();
    public abstract IServiceScreenManager getScreenManager();
    public abstract ITimeOutPollManager getTimeOutPollManager();

}
