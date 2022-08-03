package net.suqatri.redicloud.api.impl.player;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

@Getter
@Setter
public class CloudPlayer extends RBucketObject implements ICloudPlayer {


    private UUID uniqueId;
    private String name;
    private long firstLogin;
    private long lastLogin;
    private long lastLogout;
    private String lastIp;
    private UUID lastConnectedServerId;
    private UUID lastConnectedProxyId;
    private boolean connected;
    private IPlayerBridge bridge = CloudDefaultAPIImpl.getInstance().createBridge(this.getHolder());

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getServer() {
        FutureAction<IRBucketHolder<ICloudService>> futureAction = new FutureAction<>();
        if (!isConnected()) {
            futureAction.completeExceptionally(new IllegalStateException("player:" + this.getUniqueId() + " is not connected"));
            return futureAction;
        }
        CloudAPI.getInstance().getServiceManager().getServiceAsync(this.getLastConnectedServerId())
                .onFailure(futureAction)
                .onSuccess(futureAction::complete);
        return futureAction;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getProxy() {
        FutureAction<IRBucketHolder<ICloudService>> futureAction = new FutureAction<>();
        if (!isConnected()) {
            futureAction.completeExceptionally(new IllegalStateException("player:" + this.getUniqueId() + " is not connected"));
            return futureAction;
        }
        CloudAPI.getInstance().getServiceManager().getServiceAsync(this.getLastConnectedProxyId())
                .onFailure(futureAction)
                .onSuccess(futureAction::complete);
        return futureAction;
    }

}
