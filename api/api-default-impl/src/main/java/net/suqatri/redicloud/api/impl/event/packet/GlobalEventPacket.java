package net.suqatri.redicloud.api.impl.event.packet;

import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudGlobalEvent;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

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
