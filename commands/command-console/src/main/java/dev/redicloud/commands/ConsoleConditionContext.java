package dev.redicloud.commands;

public class ConsoleConditionContext extends ConditionContext<ConsoleCommandIssuer> {
    ConsoleConditionContext(ConsoleCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public CommandSender getSender() {
        return getIssuer().getIssuer();
    }

}
