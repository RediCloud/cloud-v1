package dev.redicloud.api.node.file.event;

import lombok.Data;
import dev.redicloud.api.event.CloudEvent;

import java.util.UUID;

@Data
public class FilePulledTemplatesEvent extends CloudEvent {

    private final UUID targetNodeId;
    private final boolean successfull;

}
