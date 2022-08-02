package net.suqatri.redicloud.plugin.velocity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LegacyMessageUtils {

    public static Component component(String legacyMessage){
        return LegacyComponentSerializer.legacySection().deserialize(legacyMessage);
    }

    public static String legacyText(Component component){
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

}
