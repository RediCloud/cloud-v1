package dev.redicloud.node.template;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.AnswerCompleter;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.suggester.BooleanSuggester;
import lombok.Getter;
import dev.redicloud.node.NodeLauncher;

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
