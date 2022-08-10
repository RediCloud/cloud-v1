package dev.redicloud.api.utils;

import dev.redicloud.api.network.NetworkComponentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationType {

    NODE(NetworkComponentType.NODE),
    SERVICE_MINECRAFT(NetworkComponentType.SERVICE),
    SERVICE_PROXY(NetworkComponentType.SERVICE);

    private final NetworkComponentType networkComponentType;

}
