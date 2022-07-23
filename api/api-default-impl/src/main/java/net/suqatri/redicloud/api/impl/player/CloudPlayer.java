package net.suqatri.redicloud.api.impl.player;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.player.ICloudPlayer;
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

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getServer() {
        FutureAction<IRBucketHolder<ICloudService>> futureAction = new FutureAction<>();
        if (!isConnected()) {
            futureAction.completeExceptionally(new IllegalStateException("player@" + this.getUniqueId() + " is not connected"));
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
            futureAction.completeExceptionally(new IllegalStateException("player@" + this.getUniqueId() + " is not connected"));
            return futureAction;
        }
        CloudAPI.getInstance().getServiceManager().getServiceAsync(this.getLastConnectedProxyId())
                .onFailure(futureAction)
                .onSuccess(futureAction::complete);
        return futureAction;
    }

    @Override
    public void sendMessage(String message) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendActionbar(String message) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendTab(String header, String footer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void connect(String server) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void connect(IRBucketHolder<ICloudService> cloudService) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void disconnect(String reason) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
