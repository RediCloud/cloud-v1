package dev.redicloud.commons;

import dev.redicloud.commons.function.Predicates;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * This class provides some useful methods for strings.
 */
public class StringUtils {

    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWER = UPPER.toLowerCase(Locale.ROOT);
    public static final String NUMBERS = "0123456789";
    public static final String ALL = UPPER + LOWER + NUMBERS;
    public static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Get first character of every word in a string.
     *
     * @param s
     * @return
     */
    public static String firstLetters(String s) {
        Predicates.notNull(s, "s cannot be null");

        StringBuilder builder = new StringBuilder();
        for (String t : s.split(" ")) builder.append(t.charAt(0));
        return builder.toString();
    }

    /**
     * Format a {@link Enum} to a string.
     *
     * @param e
     * @return
     */
    public static String toName(Enum e) {
        Predicates.notNull(e, "e cannot be null");

        StringBuilder builder = new StringBuilder();

        for (String s : e.name().split("_")) {
            if (!builder.toString().isEmpty()) builder.append(" ");
            builder.append(s.substring(0, 1).toUpperCase());
            builder.append(s.toLowerCase(), 1, s.length());
        }

        return builder.toString();
    }

    /**
     * Generate a random string.
     *
     * @param s      The possible characters.
     * @param length The length of the string.
     * @return The random string.
     */
    public static String randomString(String s, int length) {
        Predicates.notNull(s, "s cannot be null");
        Predicates.notNullAndIllegalArgument(length, length < 0, "length cannot be negative");

        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) builder.append(s.charAt(secureRandom.nextInt(length)));
        return builder.toString();
    }

    /**
     * Get how much percent of a string is upper case.
     *
     * @param s The string.
     * @return The percent of upper case.
     */
    public static double getUppercasePercentage(String s) {
        Predicates.notNull(s, "s cannot be null");

        double n = 0.0;
        final char[] array = s.toCharArray();
        for (int length = array.length, i = 0; i < length; ++i) {
            if (Character.isUpperCase(array[i])) {
                ++n;
            }
        }
        return n / s.length() * 100.0;
    }

    /**
     * Translate a number to a romic number.
     *
     * @param i The number.
     * @return The romic number.
     */
    public static String translateToRomicNumber(int i) {
        Predicates.notNullAndIllegalArgument(i, i < 0 || i > 5, "i cannot be negative or greater than 5");

        switch (i) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            default:
                return "0";
        }
    }

}
