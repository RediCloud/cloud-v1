package net.suqatri.cloud.api.impl.event.packet;

import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudGlobalEvent;
import net.suqatri.cloud.api.impl.packet.CloudPacket;

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
