package net.suqatri.redicloud.node.template;

import lombok.Data;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.utils.Files;

import java.io.File;

@Data
public class ServiceTemplateDeletePacket extends CloudPacket {

    private String templateName;

    @Override
    public void receive() {
        File file = new File(Files.TEMPLATE_FOLDER.getFile(), this.templateName);
        if(file.exists()){
            file.delete();
        }
    }
}
