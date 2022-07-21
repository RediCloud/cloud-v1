package net.suqatri.redicloud.api.network;

import java.io.Serializable;

public interface INetworkComponentInfo extends Serializable {

    String getKey();
    String getIdentifier();
    NetworkComponentType getType();

}
