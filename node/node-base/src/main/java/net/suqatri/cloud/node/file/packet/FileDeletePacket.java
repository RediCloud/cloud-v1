package net.suqatri.cloud.node.file.packet;

import lombok.Data;
import net.suqatri.cloud.api.impl.packet.CloudPacket;

import java.io.File;

@Data
public class FileDeletePacket extends CloudPacket {

    private String path;

    @Override
    public void receive() {
        File file = new File(this.path);
        if(file.exists()) file.delete();
    }
}
