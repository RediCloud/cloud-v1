package net.suqatri.redicloud.api.impl.service.packet.start;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.impl.packet.response.CloudPacketResponse;

import java.util.UUID;

@Getter
@Setter
public class CloudFactoryServiceStartResponseCloud extends CloudPacketResponse {

    private UUID serviceId;

}
