package com.github.multidestroy.bukkit.i18n;

import com.github.multidestroy.bukkit.Config;
import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    private static ResourceBundle resourceBundle;

    public static void loadTranslation(Config config) {
        String language;
        if ((language = config.get().getString("language")) != null) {
            Locale locale = new Locale(language);
            resourceBundle = ResourceBundle.getBundle("bukkit/messages", locale, new UTF8Control());
        } else
            resourceBundle = ResourceBundle.getBundle("bukkit/messages", new UTF8Control());
    }

    public static ResourceBundle get() {
        return resourceBundle;
    }

    public static String getColoredString(String key) {
        return ChatColor.translateAlternateColorCodes('&', resourceBundle.getString(key));
    }
}
