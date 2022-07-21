package net.suqatri.cloud.api.poll.timeout.event;

import lombok.Data;
import net.suqatri.cloud.api.event.CloudEvent;
import net.suqatri.cloud.api.event.CloudGlobalEvent;

import java.util.UUID;

@Data
public class NodeTimeOutedEvent extends CloudGlobalEvent {

    private final UUID nodeId;

    private final int passedCount;
    private final int failedCount;
    private final int errorCount;
    private final int totalCount;
    private final int connectedCount;
    private final int unconnectedCount;
    private final int min;

}
