package com.github.multidestroy;

import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    private static ResourceBundle resourceBundle;

    /**
     * @return If region typed in config was good return true, otherwise set region to enGB and return false
     */

    static void loadTranslation(Config config) {
        String language;
        if((language = config.get().getString("language")) != null) {
            Locale locale = new Locale(language);
            resourceBundle = ResourceBundle.getBundle("messages", locale);
        } else
            resourceBundle = ResourceBundle.getBundle("messages");
    }

    public static ResourceBundle get() {
        return resourceBundle;
    }

    public static String getColoredString(String key) {
        return ChatColor.translateAlternateColorCodes('&', resourceBundle.getString(key));
    }
}
