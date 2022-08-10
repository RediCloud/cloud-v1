package dev.redicloud.node.setup.group;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupHeaderBehaviour;
import dev.redicloud.node.console.setup.annotations.AnswerCompleter;
import dev.redicloud.node.console.setup.annotations.ConditionChecker;
import dev.redicloud.node.console.setup.annotations.Question;
import dev.redicloud.node.console.setup.conditions.PositivIntegerCondition;
import dev.redicloud.node.console.setup.suggester.BooleanSuggester;
import dev.redicloud.node.console.setup.suggester.MemorySuggester;
import dev.redicloud.node.setup.condition.GroupMemoryCondition;
import dev.redicloud.node.setup.condition.MinecraftServiceVersionExistsCondition;
import dev.redicloud.node.setup.suggester.CloudServiceMinecraftVersionSuggester;
import lombok.Getter;
import dev.redicloud.node.NodeLauncher;

@Getter
public class MinecraftSetup extends Setup<MinecraftSetup> {

    @Question(id = 1, question = "How many service of this group should be online all the time?")
    @ConditionChecker(value = PositivIntegerCondition.class, message = "The number of services must be positive or zero.")
    private int minServices;

    @Question(id = 2, question = "How many service of this group should be online at most?")
    private int maxServices;

    @Question(id = 3, question = "How much memory should be allocated to each service?")
    @AnswerCompleter(value = MemorySuggester.class)
    @ConditionChecker(value = GroupMemoryCondition.class, message = "The memory must be higher than 500")
    private int maxMemory;

    @Question(id = 4, question = "What is the start priority of the group?")
    private int startPriority;

    @Question(id = 5, question = "What percentage does a server need to be full for a new one to start? (-1 = disable)")
    private int percentToStartNewService;

    @Question(id = 6, question = "Should the group a static group?")
    @AnswerCompleter(value = BooleanSuggester.class)
    private boolean staticGroup;

    @Question(id = 7, question = "Should the group be a fallback group?")
    @AnswerCompleter(value = BooleanSuggester.class)
    private boolean fallback;

    @Question(id = 8, question = "What service version should be used for the group?")
    @ConditionChecker(value = MinecraftServiceVersionExistsCondition.class, message = "Service version doesn't exist.")
    @AnswerCompleter(value = CloudServiceMinecraftVersionSuggester.class)
    private String serviceVersionName;

    public MinecraftSetup() {
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
