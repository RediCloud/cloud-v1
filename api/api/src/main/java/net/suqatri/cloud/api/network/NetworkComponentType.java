package net.suqatri.cloud.api.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NetworkComponentType {

    NODE("node@"),
    SERVICE("service@");

    private final String prefix;

}
