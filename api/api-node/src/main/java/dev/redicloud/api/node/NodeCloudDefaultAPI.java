package dev.redicloud.api.node;

import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.node.CloudNode;
import dev.redicloud.api.node.service.screen.IServiceScreenManager;
import lombok.Getter;
import dev.redicloud.api.node.poll.timeout.ITimeOutPollManager;
import dev.redicloud.api.node.file.IFileTransferManager;
import dev.redicloud.api.utils.ApplicationType;

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
