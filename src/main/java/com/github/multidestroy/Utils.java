package com.github.multidestroy;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Utils {

    public static String mergeListWithNewLines(List<String> list) {
        StringBuilder newString = new StringBuilder();
        int i = 0;
        for(String line : list) {
            newString.append(line);

            if(list.size() - 1 != i++)
                newString.append('\n');
        }
        return ChatColor.translateAlternateColorCodes('&', newString.toString());
    }

    public static boolean isEmailNameLongerThan7chars(String email) {
        for(int i = 0; i < 7; i++)
            if(email.charAt(i) == '@')
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
        if(!message.contains(command)) {
            for (String alias : aliases)
                if (message.contains(alias))
                    return true;
            return false;
        }
        return true;
    }

    private static boolean startsWithCommand(String message, String command) {
        int offSet = 0;
        if (message.charAt(0) == '/')
            offSet = 1;

        if(message.startsWith(command, offSet))
            return message.length() == command.length() + offSet || message.charAt(command.length() + offSet) == ' ';
        return false;

    }

}
