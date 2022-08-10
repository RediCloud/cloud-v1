package dev.redicloud.commons;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDUtility {

    public static boolean isCracked(UUID uuid, String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).equals(uuid);
    }

    public static boolean isPremium(UUID uuid, String username) {
        return !isCracked(uuid, username);
    }

    public static boolean isBedrock(UUID uuid, String username) {
        return uuid.toString().startsWith("00000000-0000-0000-0009-");
    }

    public static UUIDType getUUIDType(UUID uuid, String username) {
        if (isBedrock(uuid, username)) {
            return UUIDType.BEDROCK;
        }
        if (isCracked(uuid, username)) {
            return UUIDType.CRACKED;
        }
        return UUIDType.PREMIUM;
    }

    public static enum UUIDType {
        BEDROCK, PREMIUM, CRACKED
    };

}
