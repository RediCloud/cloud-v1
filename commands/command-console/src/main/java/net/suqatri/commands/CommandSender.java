package net.suqatri.commands;

import lombok.Data;

@Data
public abstract class CommandSender {

    private final String name;

    public abstract void sendMessage(String message);

}
