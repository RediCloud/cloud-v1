package net.suqatri.redicloud.node.setup.group;

import lombok.Getter;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.annotations.RequiresEnum;
import net.suqatri.redicloud.node.setup.suggester.ServiceEnvironmentSuggester;

@Getter
public class GroupSetup extends Setup<GroupSetup> {

    @RequiresEnum(ServiceEnvironment.class)
    @Question(id = 1, question = "Which type of group do you want to create?")
    @AnswerCompleter(value = ServiceEnvironmentSuggester.class)
    private ServiceEnvironment environment;

    public GroupSetup() {
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
