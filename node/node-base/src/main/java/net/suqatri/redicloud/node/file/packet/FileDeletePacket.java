package net.suqatri.redicloud.node.file.packet;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.utils.ApplicationType;

import java.io.File;

@Data
public class FileDeletePacket extends CloudPacket {

    private String path;

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        File file = new File(this.path);
        if(file.exists()) file.delete();
    }
}
