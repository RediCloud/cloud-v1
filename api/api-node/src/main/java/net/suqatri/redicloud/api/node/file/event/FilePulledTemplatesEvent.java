package net.suqatri.redicloud.api.node.file.event;

import lombok.Data;
import net.suqatri.redicloud.api.event.CloudEvent;

import java.util.UUID;

@Data
public class FilePulledTemplatesEvent extends CloudEvent {

    private final UUID targetNodeId;
    private final boolean successfull;

}
