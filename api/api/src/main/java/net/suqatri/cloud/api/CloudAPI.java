package net.suqatri.cloud.api;

import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;

public abstract class CloudAPI {

    public abstract IConsole getConsole();
    public abstract ICommandManager<?> getCommandManager();

}
