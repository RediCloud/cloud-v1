package net.suqatri.commands.locales;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LocaleManager <T> {
    //private volatile Reflections resourceScanner;
    private final Function<T, Locale> localeMapper;
    private Locale defaultLocale;
    private final Map<Locale, LanguageTable> tables = new HashMap<>();

    LocaleManager(Function<T, Locale> localeMapper, Locale defaultLocale) {
        this.localeMapper = localeMapper;
        this.defaultLocale = defaultLocale;
    }

    /**
     *
     * @param localeMapper Mapper to map a context to Locale
     * @param <T> Context Class Type
     */
    public static <T> LocaleManager<T> create(@NotNull Function<T, Locale> localeMapper) {
        return new LocaleManager<>(localeMapper, Locale.ENGLISH);
    }

    /**
     *
     * @param localeMapper Mapper to map a context to Locale
     * @param defaultLocale Default Locale
     * @param <T> Context Class Type
     */
    public static <T> LocaleManager<T> create(@NotNull Function<T, Locale> localeMapper, Locale defaultLocale) {
        return new LocaleManager<>(localeMapper, defaultLocale);
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public Locale setDefaultLocale(Locale defaultLocale) {
        Locale previous = this.defaultLocale;
        this.defaultLocale = defaultLocale;
        return previous;
    }

    /**
     * If a list of locales is supplied, loads the matching message bundle for each locale.
     * If none are supplied, just the default locale is loaded.
     */
    public boolean addMessageBundle(@NotNull String bundleName, @NotNull Locale... locales) {
        return this.addMessageBundle(this.getClass().getClassLoader(), bundleName, locales);
    }

    public boolean addMessageBundle(@NotNull ClassLoader classLoader, @NotNull String bundleName, @NotNull Locale... locales) {
        if (locales.length == 0) {
            locales = new Locale[] {defaultLocale};
        }
        boolean found = false;
        for (Locale locale : locales) {
            if (getTable(locale).addMessageBundle(classLoader, bundleName)) {
                //System.out.println("Loaded " + bundleName+":" + locale + " from " + classLoader.toString());
                found = true;
            }
        }
        return found;
    }

    public void addMessages(@NotNull Locale locale, @NotNull Map<MessageKey, String> messages) {
        getTable(locale).addMessages(messages);
    }

    public String addMessage(@NotNull Locale locale, @NotNull MessageKey key, @NotNull String message) {
        return getTable(locale).addMessage(key, message);
    }

    public String getMessage(T context, @NotNull MessageKey key) {
        Locale locale = localeMapper.apply(context);

        String message = getTable(locale).getMessage(key);
        if (message == null && !locale.getCountry().isEmpty()) {
            message = getTable(new Locale(locale.getLanguage())).getMessage(key);
        }

        if (message == null && !Objects.equals(locale, defaultLocale)) {
            message = getTable(defaultLocale).getMessage(key);
        }

        return message;
    }

    public @NotNull LanguageTable getTable(@NotNull Locale locale) {
        return tables.computeIfAbsent(locale, LanguageTable::new);
    }

    public boolean addResourceBundle(ResourceBundle bundle, Locale locale) {
        return this.getTable(locale).addResourceBundle(bundle);
    }

}
