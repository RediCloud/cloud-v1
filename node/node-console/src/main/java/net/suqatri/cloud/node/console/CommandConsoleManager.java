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

    public CommandConsoleManager() {
        this.messages = new HashMap<>();
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
        addLanguageKey(MessageKeys.PERMISSION_DENIED, "%prefix%§cDafür hast du keine Rechte!");
        addLanguageKey(MessageKeys.PERMISSION_DENIED_PARAMETER, "%prefix%§cDafür hast du keine Rechte!");
        addLanguageKey(MessageKeys.ERROR_GENERIC_LOGGED, "%prefix%§cEs ist ein Fehler aufgetreten!");
        addLanguageKey(MessageKeys.UNKNOWN_COMMAND, "%prefix%§cUnbekannter Befehl! /help");
        addLanguageKey(MessageKeys.INVALID_SYNTAX, "%prefix%§cFalsch: §7{command} {syntax}");
        addLanguageKey(MessageKeys.ERROR_PREFIX, "Error: {message}");
        addLanguageKey(MessageKeys.ERROR_PERFORMING_COMMAND, "%prefix%§cEs ist ein Fehler aufgetreten!");
        addLanguageKey(MessageKeys.INFO_MESSAGE, "{message}");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_ONE_OF, "Error: Please specify one of (<c2>{valid}</c2>).");
        addLanguageKey(MessageKeys.MUST_BE_A_NUMBER, "%prefix%§c{num} muss eine Zahl sein!");
        addLanguageKey(MessageKeys.MUST_BE_MIN_LENGTH, "%prefix%§cDas Argument muss min. {min} Zeichen lang sein!");
        addLanguageKey(MessageKeys.MUST_BE_MAX_LENGTH, "%prefix%§cDas Argument darf max. {max} Zeichen lang sein!");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_AT_MOST, "%prefix%§cDie Zahl darf nicht größer als {max} sein!");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "%prefix%§cDie Zahl darf nicht kleiner als {min} sen!");
        addLanguageKey(MessageKeys.NOT_ALLOWED_ON_CONSOLE, "%prefix%§cDu musst ein Spieler sein!");
        addLanguageKey(MessageKeys.COULD_NOT_FIND_PLAYER, "%prefix%§cEs konnte kein Spieler gefunden werden mit dem Namen %c1%{search}!");
        addLanguageKey(MessageKeys.NO_COMMAND_MATCHED_SEARCH, "%prefix%§cEs wurde kein Befehl mit dem Namen {search} gefunden!");
        addLanguageKey(MessageKeys.HELP_PAGE_INFORMATION, "§8§m--§8> §7Hilfe %c1%{page}§8/%c2%{totalpages}§7");
        addLanguageKey(MessageKeys.HELP_NO_RESULTS, "%prefix%§cEs wurden keinen weiteren Seiten gefunden!");
        addLanguageKey(MessageKeys.HELP_HEADER, "§8<§m------------|§7 Hilfe für %c1%{commandprefix}{command} §8§m-|§m------------§8>");
        addLanguageKey(MessageKeys.HELP_FORMAT, "%c1%{commandprefix}{command} {parameters} §8| §7{description}");
        addLanguageKey(MessageKeys.HELP_DETAILED_HEADER, "§8<§m------------|§7 Hilfe für %c1%{commandprefix}{command} §8§m-|§m------------§8>");
        addLanguageKey(MessageKeys.HELP_DETAILED_COMMAND_FORMAT, "%c1%{commandprefix}{command} {parameters} §8| §7{description}");
        addLanguageKey(MessageKeys.HELP_DETAILED_PARAMETER_FORMAT, "%c1%{syntaxorname}: §7{description}");
        addLanguageKey(MessageKeys.HELP_SEARCH_HEADER, "§8<§m------------|§7 Hilfe für %c1%{commandprefix}{command} §8§m-|§m------------§8>");
        saveLanguage(Locales.GERMAN);
        getLocales().setDefaultLocale(Locales.GERMAN);
    }

    public void saveLanguage(Locale locale){
        this.getLocales().addMessages(locale, messages);
    }

    public void addLanguageKey(MessageKeys keys, String value){
        messages.put(keys, value);
    }
}
