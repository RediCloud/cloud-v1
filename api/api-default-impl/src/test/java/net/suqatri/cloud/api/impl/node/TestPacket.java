package net.suqatri.cloud.api.impl.node;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class TestPacket implements Serializable {

    private UUID uniqueId;

}
