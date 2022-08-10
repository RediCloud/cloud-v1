package dev.redicloud.api.impl.event.packet;

import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudGlobalEvent;
import dev.redicloud.api.impl.packet.CloudPacket;

@Setter
public class GlobalEventPacket<E extends CloudGlobalEvent> extends CloudPacket {

    private E event;

    public E getEvent() {
        return this.event;
    }

    @Override
    public void receive() {
        CloudAPI.getInstance().getEventManager().postLocal(this.event);
    }
}
