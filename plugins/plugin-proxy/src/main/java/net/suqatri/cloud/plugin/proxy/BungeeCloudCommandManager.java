package net.suqatri.cloud.plugin.proxy;

import net.md_5.bungee.api.plugin.Plugin;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.commands.BaseCommand;
import net.suqatri.commands.BungeeCommandManager;
import net.suqatri.commands.Locales;
import net.suqatri.commands.MessageKeys;
import net.suqatri.commands.locales.MessageKeyProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BungeeCloudCommandManager extends BungeeCommandManager implements ICommandManager<BaseCommand>{

    private final Map<MessageKeyProvider, String> messages;

    public BungeeCloudCommandManager(Plugin plugin) {
        super(plugin);
        this.messages = new HashMap<>();
        this.enableUnstableAPI("help");
        this.loadDefaultLanguage();
    }


    private void loadDefaultLanguage(){
        addLanguageKey(MessageKeys.PERMISSION_DENIED, "§cYou don't have permission to use this command.");
        addLanguageKey(MessageKeys.PERMISSION_DENIED_PARAMETER, "§cYou don't have permission to use this parameter!");
        addLanguageKey(MessageKeys.ERROR_GENERIC_LOGGED, "§cError: %error%");
        addLanguageKey(MessageKeys.UNKNOWN_COMMAND, "§7Unknown command! Use §b\"help\" §7for help.");
        addLanguageKey(MessageKeys.INVALID_SYNTAX, "§7Usage: §b{command} {syntax}");
        addLanguageKey(MessageKeys.ERROR_PREFIX, "§cError: {message}");
        addLanguageKey(MessageKeys.ERROR_PERFORMING_COMMAND, "§cError while performing command: {message}");
        addLanguageKey(MessageKeys.INFO_MESSAGE, "{message}");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_ONE_OF, "§cError: Please specify one of {valid}");
        addLanguageKey(MessageKeys.MUST_BE_A_NUMBER, "%prefix%§c{num} muss eine Zahl sein!");
        addLanguageKey(MessageKeys.MUST_BE_MIN_LENGTH, "§cThe argument must be min. {min} characters long!");
        addLanguageKey(MessageKeys.MUST_BE_MAX_LENGTH, "§cThe argument must be max. {max} characters long!");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_AT_MOST, "§cThe argument must be at most {max}.");
        addLanguageKey(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "§cThe number must be at least {min}.");
        addLanguageKey(MessageKeys.NOT_ALLOWED_ON_CONSOLE, "§cNOT_SET: NOT_ALLOWED_ON_CONSOLE");
        addLanguageKey(MessageKeys.COULD_NOT_FIND_PLAYER, "§cCould not find player {player}");
        addLanguageKey(MessageKeys.NO_COMMAND_MATCHED_SEARCH, "§cNo matching command found for {search}!");
        addLanguageKey(MessageKeys.HELP_PAGE_INFORMATION, "§8--> §7Help §7{page}§8/§b{totalpages}§7");
        addLanguageKey(MessageKeys.HELP_NO_RESULTS, "§7There is no further help available.");
        addLanguageKey(MessageKeys.HELP_HEADER, "§8<------------§8|§7 Help for §b{commandprefix}§7{command} §8|------------§8>");
        addLanguageKey(MessageKeys.HELP_FORMAT, "§b{commandprefix}{command} §7{parameters} §8| §7{description}");
        addLanguageKey(MessageKeys.HELP_DETAILED_HEADER, "§8§m<------------§8|§7 Help for §b{commandprefix}§7{command} §8§|------------§8>");
        addLanguageKey(MessageKeys.HELP_DETAILED_COMMAND_FORMAT, "§b{commandprefix}{command} §7{parameters} §8| §7{description}");
        addLanguageKey(MessageKeys.HELP_DETAILED_PARAMETER_FORMAT, "§b{syntaxorname}: §7{description}");
        addLanguageKey(MessageKeys.HELP_SEARCH_HEADER, "§8<------------§8|§7 Hilfe für §b{commandprefix}§7{command} §8|-----------§8>");
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
