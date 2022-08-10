package dev.redicloud.commands.locales;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public class LanguageTable {

    private final Locale locale;
    private final Map<MessageKey, String> messages = new HashMap<>();

    LanguageTable(Locale locale) {
        this.locale = locale;
    }

    public String addMessage(MessageKey key, String message) {
        return messages.put(key, message);
    }

    public String getMessage(MessageKey key) {
        return messages.get(key);
    }

    public void addMessages(@NotNull Map<MessageKey, String> messages) {
        this.messages.putAll(messages);
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean addMessageBundle(String bundleName) {
        return this.addMessageBundle(this.getClass().getClassLoader(), bundleName);
    }

    public boolean addMessageBundle(ClassLoader classLoader, String bundleName) {
        try {
            return this.addResourceBundle(ResourceBundle.getBundle(bundleName, this.locale, classLoader, new UTF8Control()));
        } catch (MissingResourceException e) {
            return false;
        }
    }

    public boolean addResourceBundle(ResourceBundle bundle) {
        for (String key : bundle.keySet()) {
            addMessage(MessageKey.of(key), bundle.getString(key));
        }

        return !bundle.keySet().isEmpty();
    }
}
