package net.suqatri.cloud.api.impl.node;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

@Getter
public class CloudNode extends ObjectHandler {

    private boolean connected;
    private String hostname;
    private long lastConnection;
    private long lastStart;
    private int maxMemory;
    private int maxParallelStartingServiceCount;
    private int maxServiceCount;
    private int memoryUsage;
    private String name;
    private Collection<String> startedServiceUniqueIds;
    private UUID uniqueId;

}
