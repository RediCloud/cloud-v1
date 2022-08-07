package net.suqatri.redicloud.api.impl.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.password.UpdatableBCrypt;

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
    private boolean cracked = false;
    private String passwordHash;
    private int passwordLogRounds = 0;
    @JsonIgnore
    private IPlayerBridge bridge = CloudDefaultAPIImpl.getInstance().createBridge(this);
    @JsonIgnore
    private UpdatableBCrypt bcrypt;

    public boolean verifyPassword(String password) {
        return bcrypt.verifyHash(password, this.passwordHash);
    }

    public void setPassword(String password) {
        this.passwordHash = bcrypt.hash(password);
    }

    @Override
    public void init() {
        this.bcrypt = new UpdatableBCrypt(this.passwordLogRounds);
    }

    @Override
    public FutureAction<ICloudService> getServer() {
        FutureAction<ICloudService> futureAction = new FutureAction<>();
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
    public FutureAction<ICloudService> getProxy() {
        FutureAction<ICloudService> futureAction = new FutureAction<>();
        if (!isConnected()) {
            futureAction.completeExceptionally(new IllegalStateException("player:" + this.getUniqueId() + " is not connected"));
            return futureAction;
        }
        CloudAPI.getInstance().getServiceManager().getServiceAsync(this.getLastConnectedProxyId())
                .onFailure(futureAction)
                .onSuccess(futureAction::complete);
        return futureAction;
    }

    @Override
    public String getIdentifier() {
        return this.uniqueId.toString();
    }
}
