package com.github.multidestroy.bukkit;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Utils {

    public static boolean isEmailNameLongerThan7chars(String email) {
        for (int i = 0; i < 7; i++)
            if (email.charAt(i) == '@')
                return false;
        return true;
    }

    public static boolean isCommand(String message, String commandName, JavaPlugin plugin) {
        List<String> aliases = plugin.getCommand(commandName).getAliases();
        if (!startsWithCommand(message, commandName)) {
            for (String alias : aliases)
                if (startsWithCommand(message, alias))
                    return true;
            return false;
        }
        return true;
    }

    static boolean containsCommand(String message, String command, JavaPlugin plugin) {
        List<String> aliases = plugin.getCommand(command).getAliases();
        int indexOfSlash = message.indexOf('/');
        if (indexOfSlash != -1)
            message = message.substring(indexOfSlash + 1);
        else
            return false;
        if (!message.startsWith(command + " ")) {
            for (String alias : aliases) {
                if (message.contains(alias + " ") || (message.startsWith(alias) && message.length() == alias.length()))
                    return true;
            }
            return false;
        }
        return true;
    }

    private static boolean startsWithCommand(String message, String command) {
        int offSet = 0;
        if (message.charAt(0) == '/')
            offSet = 1;

        if (message.startsWith(command, offSet))
            return message.length() == command.length() + offSet || message.charAt(command.length() + offSet) == ' ';
        return false;

    }

}
