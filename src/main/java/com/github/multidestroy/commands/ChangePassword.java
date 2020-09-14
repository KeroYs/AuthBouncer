package com.github.multidestroy.commands;

import com.github.multidestroy.PasswordHasher;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerActivityStatus;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;

public class ChangePassword implements CommandExecutor {

    private PluginSystem system;
    private Database database;
    private LoginAttemptEvent loginAttemptEvent;
    private String successfulUsage;
    private PasswordHasher passwordHasher;
    private JavaPlugin plugin;
    private ThreadSystem threadSystem;
    public static String correctUsage = ChatColor.RED + "/changepassword <curr_pass> <new_pass> <new_pass>";

    public ChangePassword(PluginSystem system, Database database, Config config, PasswordHasher passwordHasher, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.successfulUsage = config.getMessage("changepassword");
        this.passwordHasher = passwordHasher;
        this.threadSystem = threadSystem;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length == 3) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncTask((Player) sender, args[0], args[1], args[2]));
            } else
                sender.sendMessage(correctUsage);
        } else
            sender.sendMessage(ChatColor.RED + "Command is not available to use by the console!");
        return false;
    }

    private Runnable asyncTask(Player player, String currPass, String newPass1, String newPass2) {
        return () -> {
            String hashedNewPassword;
            PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
            PlayerActivityStatus playerActivityStatus = null;

            if(threadSystem.tryLock(player.getName())) {
                try {
                    if(system.isPasswordCorrect(playerInfo, currPass)) {
                        if(PluginSystem.isPasswordPossible(newPass1)) {
                            if(newPass1.equals(newPass2)) {
                                hashedNewPassword = passwordHasher.hashPassword(newPass1);
                                if(database.changePassword(player.getName(), hashedNewPassword)) {
                                    //if password was successfully saved in the database
                                    playerInfo.setHashedPassword(hashedNewPassword);
                                    player.sendMessage(successfulUsage);
                                    playerActivityStatus = PlayerActivityStatus.PASSWORD_CHANGE;
                                } else
                                    player.sendMessage(ChatColor.RED + "Password could not be changed! Try a few minutes later.");
                            } else
                                player.sendMessage(ChatColor.RED + "Passwords are not equals!");
                        } else
                            player.sendMessage(ChatColor.RED + "Password needs to be long at least 5 chars!");
                    } else
                        player.sendMessage(ChatColor.RED + "Current password is not correct");
                } finally {
                    threadSystem.unlock(player.getName());
                }
            } else
                player.sendMessage(ChatColor.RED + "Wait until previous command is done!");

            if(playerActivityStatus != null)
                database.saveLoginAttempt(player, playerActivityStatus, Instant.now());
        };
    }
}
