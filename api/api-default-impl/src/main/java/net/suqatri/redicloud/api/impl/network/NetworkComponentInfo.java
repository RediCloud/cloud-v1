package net.suqatri.redicloud.api.impl.network;

import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.network.NetworkComponentType;

import java.util.Objects;

public class NetworkComponentInfo implements INetworkComponentInfo {

    private final String identifier;
    private final NetworkComponentType type;

    public NetworkComponentInfo(NetworkComponentType type, String identifier) {
        this.identifier = identifier;
        this.type = type;
    }

    public NetworkComponentInfo(){
        this.identifier = null;
        this.type = null;
    }

    @Override
    public String getKey() {
        return this.type.getPrefix() + this.identifier;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public NetworkComponentType getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkComponentInfo that = (NetworkComponentInfo) o;
        return Objects.equals(this.identifier, that.identifier) && this.type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identifier, this.type);
    }
}
