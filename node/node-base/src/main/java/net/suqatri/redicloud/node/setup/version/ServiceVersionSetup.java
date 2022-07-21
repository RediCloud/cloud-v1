package net.suqatri.redicloud.node.setup.version;

import lombok.Getter;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.annotations.RequiresEnum;
import net.suqatri.redicloud.node.NodeLauncher;

@Getter
public class ServiceVersionSetup extends Setup<ServiceVersionSetup> {

    @Question(id = 1, question = "What is the environment type of this version?")
    @RequiresEnum(ServiceEnvironment.class)
    private ServiceEnvironment environment;

    @Question(id = 2, question = "What is the download url of this version?")
    private String downloadUrl;

    @Question(id = 3, question = "Is this version a paper clip?")
    private boolean paperClip;

    public ServiceVersionSetup() {
        super(NodeLauncher.getInstance().getConsole());
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }
}
