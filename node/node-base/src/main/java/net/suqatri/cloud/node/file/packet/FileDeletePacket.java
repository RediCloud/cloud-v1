package net.suqatri.cloud.node.file.packet;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.utils.ApplicationType;

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
