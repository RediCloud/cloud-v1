/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.suqatri.commands;

import net.suqatri.commands.apachecommonslang.ApacheCommonsExceptionUtil;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleCommandManager extends CommandManager<
        CommandSender,
        ConsoleCommandIssuer,
        Color,
        ConsoleMessageFormatter,
        ConsoleCommandExecutionContext,
        ConsoleConditionContext
        > {


    protected Map<String, ConsoleRootCommand> registeredCommands = new HashMap<>();
    protected ConsoleCommandContexts contexts;
    protected ConsoleCommandCompletions completions;
    private Locales locales;

    public ConsoleCommandManager() {
        this.formatters.put(MessageType.ERROR, defaultFormatter = new ConsoleMessageFormatter(Color.RED, Color.YELLOW, Color.RED));
        this.formatters.put(MessageType.SYNTAX, new ConsoleMessageFormatter(Color.YELLOW, Color.GREEN, Color.WHITE));
        this.formatters.put(MessageType.INFO, new ConsoleMessageFormatter(Color.BLUE, Color.GREEN, Color.GREEN));
        this.formatters.put(MessageType.HELP, new ConsoleMessageFormatter(Color.CYAN, Color.GREEN, Color.YELLOW));
        this.locales = new Locales(this);
    }

    @Override
    public synchronized CommandContexts<ConsoleCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new ConsoleCommandContexts(this);
        }
        return contexts;
    }

    @Override
    public synchronized CommandCompletions<ConsoleCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new ConsoleCommandCompletions(this);
        }
        return completions;
    }

    @Override
    public Locales getLocales() {
        return this.locales;
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            ConsoleRootCommand consoleCommand = (ConsoleRootCommand) entry.getValue();
            consoleCommand.isRegistered = true;
            if (!(command instanceof ConsoleCommand)) {
                throw new IllegalArgumentException("BaseCommand must be a ConsoleCommand otherwise it cannot be registered");
            }
            registeredCommands.put(commandName, consoleCommand);
        }
    }

    public void unregisterCommand(BaseCommand command) {
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            ConsoleRootCommand consoleCommand = (ConsoleRootCommand) entry.getValue();
            consoleCommand.getSubCommands().values().removeAll(command.subCommands.values());
            if (consoleCommand.getSubCommands().isEmpty() && consoleCommand.isRegistered) {
                consoleCommand.isRegistered = false;
                registeredCommands.remove(commandName);
            }
        }
    }


    @Override
    public boolean hasRegisteredCommands() {
        return !registeredCommands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> aClass) {
        return CommandSender.class.isAssignableFrom(aClass);
    }

    @Override
    public ConsoleCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof CommandSender)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        }
        return new ConsoleCommandIssuer(this, (CommandSender) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new ConsoleRootCommand(this, cmd);
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(registeredCommands.values());
    }

    @Override
    public ConsoleCommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new ConsoleCommandExecutionContext(command, parameter, (ConsoleCommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new ConsoleCommandCompletionContext(command, (ConsoleCommandIssuer) sender, input, config, args);
    }

    @Override
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new RegisteredCommand(command, cmdName, method, prefSubCommand);
    }

    @Override
    public ConsoleConditionContext createConditionContext(CommandIssuer issuer, String config) {
        return new ConsoleConditionContext((ConsoleCommandIssuer) issuer, config);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Logger logger = Logger.getGlobal(); //TODO
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }


    @Override
    public String getCommandPrefix(CommandIssuer issuer) {
        return issuer.isPlayer() ? "/" : "";
    }
}
