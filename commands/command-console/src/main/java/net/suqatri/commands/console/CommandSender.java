package net.suqatri.commands.console;

import lombok.Data;

@Data
public abstract class CommandSender {

    private final String name;

    public abstract void sendMessage(String message);

}
