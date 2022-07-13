package net.suqatri.cloud.node.setup.group;

import lombok.Data;
import lombok.Getter;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.commons.function.BiSupplier;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.Setup;
import net.suqatri.cloud.node.console.setup.annotations.ConditionChecker;
import net.suqatri.cloud.node.console.setup.annotations.Question;
import net.suqatri.cloud.node.console.setup.conditions.PositivIntegerCondition;

@Getter
public class GroupSetup extends Setup<GroupSetup> {

    @Question(id = 1, question = "Which type of group do you want to create?")
    private ServiceEnvironment environment;

    @Question(id = 2, question = "How many service of this group should be online all the time?")
    @ConditionChecker(value = PositivIntegerCondition.class, message = "The number of services must be positive or zero.")
    private int minServices;

    @Question(id = 3, question = "How many service of this group should be online at most?")
    private int maxServices;

    @Question(id = 4, question = "How much memory should be allocated to each service?")
    private int maxMemory;

    @Question(id = 5, question = "What is the start priority of the group?")
    private int startPriority;

    @Question(id = 6, question = "Should the group a static group?")
    private boolean staticGroup;


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
}
