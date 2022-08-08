package net.suqatri.redicloud.api.node;

import lombok.Getter;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.node.CloudNode;
import net.suqatri.redicloud.api.node.poll.timeout.ITimeOutPollManager;
import net.suqatri.redicloud.api.node.file.IFileTransferManager;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreenManager;
import net.suqatri.redicloud.api.utils.ApplicationType;

public abstract class NodeCloudDefaultAPI extends CloudDefaultAPIImpl<CloudNode> {

    @Getter
    private static NodeCloudDefaultAPI instance;

    public NodeCloudDefaultAPI() {
        super(ApplicationType.NODE);
        instance = this;
    }

    public abstract CloudNode getNode();

    public abstract IFileTransferManager getFileTransferManager();

    public abstract IServiceScreenManager getScreenManager();

    public abstract ITimeOutPollManager getTimeOutPollManager();

}
