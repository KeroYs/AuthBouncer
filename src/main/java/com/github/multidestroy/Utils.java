package com.github.multidestroy;

import net.md_5.bungee.api.ChatColor;

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

    public static boolean isCommand(String command, String commandName) {
        command = command.toLowerCase();
        int i = 0;
        if(command.length() < commandName.length())
            return false;

        while(commandName.length() != i) {
            if (command.charAt(i) != commandName.charAt(i))
                return false;
            i++;
        }
        return true;
    }

}
