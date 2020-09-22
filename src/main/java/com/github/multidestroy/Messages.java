package com.github.multidestroy;

import com.github.multidestroy.configs.Config;
import org.bukkit.ChatColor;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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
