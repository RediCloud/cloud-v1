package dev.redicloud.node.setup.group;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.AnswerCompleter;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.annotations.RequiresEnum;
import lombok.Getter;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.node.NodeLauncher;
import dev.redicloud.node.setup.suggester.ServiceEnvironmentSuggester;

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
        return SetupHeaderBehaviour.NOTHING;
    }
}
