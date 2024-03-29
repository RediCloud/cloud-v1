package dev.redicloud.api.impl.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.redicloud.api.impl.redis.bucket.RBucketObject;
import dev.redicloud.api.impl.redis.bucket.fetch.RBucketFetchAble;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.commons.password.UpdatableBCrypt;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class CloudPlayer extends RBucketFetchAble implements ICloudPlayer {


    private UUID uniqueId;
    private String name;
    private long firstLogin;
    private long lastLogin;
    private long lastLogout;
    private String lastIp;
    private UUID lastConnectedServerId;
    private UUID lastConnectedProxyId;
    private boolean connected;
    @JsonIgnore
    private IPlayerBridge bridge;

    @JsonIgnore
    private UpdatableBCrypt bcrypt;
    private boolean cracked = false;
    private String passwordHash;
    private int passwordLogRounds = 10;
    private String sessionIp;

    @JsonIgnore
    @Override
    public boolean isLoggedIn() {
        if(!this.cracked) return true;
        if(this.sessionIp == null) return false;
        if(!this.sessionIp.equals(this.lastIp)) return false;
        if(!this.isConnected()){
            return System.currentTimeMillis()-this.lastLogout < TimeUnit.MINUTES.toMillis(5);
        }
        return true;
    }

    public boolean verifyPassword(String password) {
        return this.bcrypt.verifyHash(password, this.passwordHash);
    }

    public void setPassword(String password) {
        if(this.bcrypt == null) this.bcrypt = new UpdatableBCrypt(this.passwordLogRounds);
        this.passwordHash = this.bcrypt.hash(password);
    }

    @Override
    public void init() {
        this.bcrypt = new UpdatableBCrypt(this.passwordLogRounds);
        this.bridge = CloudDefaultAPIImpl.getInstance().createBridge(this);
    }

    public void setPasswordLogRounds(int passwordLogRounds) {
        this.passwordLogRounds = passwordLogRounds;
        if(this.bcrypt == null) this.bcrypt = new UpdatableBCrypt(this.passwordLogRounds);
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

    @Override
    public String getFetchKey() {
        return this.name.toLowerCase();
    }

    @Override
    public String getFetchValue() {
        return this.uniqueId.toString();
    }
}
