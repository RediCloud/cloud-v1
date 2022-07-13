package net.suqatri.cloud.node.console;

import lombok.Getter;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.ConsoleCommandManager;
import net.suqatri.commands.Locales;
import net.suqatri.commands.MessageKeys;
import net.suqatri.commands.locales.MessageKeyProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public class CommandConsoleManager extends ConsoleCommandManager implements ICommandManager<ConsoleCommand> {

    private CommandSender commandSender;
    private final Map<MessageKeyProvider, String> messages;
    private NodeConsole nodeConsole;

    public CommandConsoleManager() throws Exception {
        this.messages = new HashMap<>();
        this.nodeConsole = createNodeConsole();
        this.loadDefaultLanguage();
        this.commandSender = new NodeConsoleSender(this);
        this.enableUnstableAPI("help");
    }

    public NodeConsole createNodeConsole() throws Exception {
        if(this.nodeConsole != null) throw new IllegalStateException("NodeConsole already created");
        this.nodeConsole = new NodeConsole(this);
        return this.nodeConsole;
    }

    @Override
    public void registerCommand(ConsoleCommand command) {
        super.registerCommand(command);
    }

    @Override
    public void unregisterCommand(ConsoleCommand command) {
        super.unregisterCommand(command);
    }

    private void loadDefaultLanguage(){
        addLanguageKey(MessageKeys.PERMISSION_DENIED, "You don't have permission to use this command.");
        addLanguageKey(MessageKeys.PERMISSION_DENIED_PARAMETER, "You don't have permission to use this parameter!");
        addLanguageKey(MessageKeys.ERROR_GENERIC_LOGGED, "Error: %error%");
        addLanguageKey(MessageKeys.UNKNOWN_COMMAND, "Unknown command! Use %hc\"help\" %tcfor help.");
        addLanguageKey(MessageKeys.INVALID_SYNTAX, "%tcUsage: %hc{command} {syntax}");
        addLanguageKey(MessageKeys.ERROR_PREFIX, "Error: {message}");
        addLanguageKey(MessageKeys.ERROR_PERFORMING_COMMAND, "Error while performing command: {message}");
        addLanguageKey(MessageKeys.INFO_MESSAGE, "{message}");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_ONE_OF, "Error: Please specify one of {valid}");
        addLanguageKey(MessageKeys.MUST_BE_A_NUMBER, "%prefix%§c{num} muss eine Zahl sein!");
        addLanguageKey(MessageKeys.MUST_BE_MIN_LENGTH, "The argument must be min. {min} characters long!");
        addLanguageKey(MessageKeys.MUST_BE_MAX_LENGTH, "The argument must be max. {max} characters long!");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_AT_MOST, "The argument must be at most {max}.");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "The number must be at least {min}.");
        addLanguageKey(MessageKeys.NOT_ALLOWED_ON_CONSOLE, "NOT_SET: NOT_ALLOWED_ON_CONSOLE");
        addLanguageKey(MessageKeys.COULD_NOT_FIND_PLAYER, "Could not find player {player}");
        addLanguageKey(MessageKeys.NO_COMMAND_MATCHED_SEARCH, "§cNo matching command found for {search}!");
        addLanguageKey(MessageKeys.HELP_PAGE_INFORMATION, "§8--> §7Help %tc{page}§8/%hc{totalpages}§7");
        addLanguageKey(MessageKeys.HELP_NO_RESULTS, "There is no further help available.");
        addLanguageKey(MessageKeys.HELP_HEADER, "§8<------------|§7 Help for %hc{commandprefix}%tc{command} §8|------------§8>");
        addLanguageKey(MessageKeys.HELP_FORMAT, "%hc{commandprefix}{command} %tc{parameters} §8| %tc{description}");
        addLanguageKey(MessageKeys.HELP_DETAILED_HEADER, "§8<------------|%tc Help for %hc{commandprefix}%tc{command} §8§|------------§8>");
        addLanguageKey(MessageKeys.HELP_DETAILED_COMMAND_FORMAT, "%hc{commandprefix}{command} %tc{parameters} §8| %tc{description}");
        addLanguageKey(MessageKeys.HELP_DETAILED_PARAMETER_FORMAT, "%hc{syntaxorname}: %tc{description}");
        addLanguageKey(MessageKeys.HELP_SEARCH_HEADER, "§8<------------|%tc Hilfe für %hc{commandprefix}%tc{command} §8|-----------§8>");
        saveLanguage(Locales.ENGLISH);
        getLocales().setDefaultLocale(Locales.ENGLISH);
    }

    public void saveLanguage(Locale locale){
        this.getLocales().addMessages(locale, messages);
    }

    public void addLanguageKey(MessageKeys keys, String value){
        messages.put(keys, value);
    }
}
