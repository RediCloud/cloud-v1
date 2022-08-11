package dev.redicloud.api.network;

import java.io.Serializable;
import java.util.UUID;

public interface INetworkComponentInfo extends Serializable {

    String getKey();

    UUID getIdentifier();

    NetworkComponentType getType();

}
