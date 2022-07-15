package net.suqatri.cloud.api.impl.service.packet.start;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.impl.packet.response.CloudPacketResponse;

import java.util.UUID;

@Getter @Setter
public class CloudFactoryServiceStartResponseCloud extends CloudPacketResponse {

    private UUID serviceId;

}
