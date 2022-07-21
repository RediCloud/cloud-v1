package net.suqatri.redicloud.node.template;

import lombok.Getter;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupHeaderBehaviour;
import net.suqatri.redicloud.node.console.setup.annotations.AnswerCompleter;
import net.suqatri.redicloud.node.console.setup.annotations.Question;
import net.suqatri.redicloud.node.console.setup.suggester.BooleanSuggester;

@Getter
public class SyncTemplateQuestion extends Setup<SyncTemplateQuestion> {

    @Question(id = 1, question = "Should the node sync templates from the cloud?")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean syncTemplates;

    public SyncTemplateQuestion() {
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
    public String getPrefix() {
        return "QUESTION";
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }
}
