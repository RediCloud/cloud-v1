package dev.redicloud.api.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum PacketChannel {

    NODE("node"),
    GLOBAL("global");

    private String name;

}
