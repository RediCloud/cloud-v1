package net.suqatri.cloud.api.node.file.event;

import lombok.Data;
import net.suqatri.cloud.api.event.CloudEvent;

import java.util.UUID;

@Data
public class FilePullTemplatesEvent extends CloudEvent {

    private final UUID targetNodeId;

}
