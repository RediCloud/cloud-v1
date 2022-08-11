package dev.redicloud.api.impl.network;

import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.network.NetworkComponentType;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class NetworkComponentInfo implements INetworkComponentInfo {

    private final UUID identifier;
    private final NetworkComponentType type;

    public NetworkComponentInfo(NetworkComponentType type, UUID identifier) {
        this.identifier = identifier;
        this.type = type;
    }

    public NetworkComponentInfo() {
        this.identifier = null;
        this.type = null;
    }

    @Override
    public String getKey() {
        return this.type.getPrefix() + this.identifier;
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
