package dev.redicloud.api.impl.service.packet.start;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.impl.packet.response.CloudPacketResponse;

import java.util.UUID;

@Getter
@Setter
public class CloudFactoryServiceStartResponse extends CloudPacketResponse {

    private UUID serviceId;

}
