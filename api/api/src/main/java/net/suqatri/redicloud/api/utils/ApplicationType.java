package net.suqatri.redicloud.api.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.suqatri.redicloud.api.network.NetworkComponentType;

@AllArgsConstructor
@Getter
public enum ApplicationType {

    NODE(NetworkComponentType.NODE),
    SERVICE_MINECRAFT(NetworkComponentType.SERVICE),
    SERVICE_PROXY(NetworkComponentType.SERVICE);

    private final NetworkComponentType networkComponentType;

}
