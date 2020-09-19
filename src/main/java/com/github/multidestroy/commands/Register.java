package com.github.multidestroy.commands;

import com.github.multidestroy.PasswordHasher;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerActivityStatus;
import com.github.multidestroy.player.PlayerGlobalRank;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
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

public class Register implements CommandExecutor {

    private Database database;
    private PluginSystem system;
    private String successfulUsage;
    private PasswordHasher passwordHasher;
    private Settings settings;
    private ThreadSystem threadSystem;
    private JavaPlugin plugin;
    public final static String correctUsage = ChatColor.RED + "/register <password> <password>";

    public Register(PluginSystem system, Database database, Config config, PasswordHasher passwordHasher, Settings settings, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.successfulUsage = config.getMessage("register");
        this.passwordHasher = passwordHasher;
        this.settings = settings;
        this.threadSystem = threadSystem;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            if (!system.isPlayerRegistered(sender.getName())) {
                if (args.length == 2) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncTask((Player) sender, args[0], args[1]));
                } else
                    sender.sendMessage(correctUsage);
            } else
                sender.sendMessage(ChatColor.RED + "You are already registered!");
        } else
            sender.sendMessage(ChatColor.RED + "Command is not available to use by the console!");
        return false;
    }

    private Runnable asyncTask(Player player, String arg1, String arg2) {
        return () -> {
            PlayerActivityStatus playerActivityStatus = null;
            PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
            if (threadSystem.tryLock(player.getName())) {
                try {
                    if (PluginSystem.isPasswordPossible(arg1)) {
                        if (arg1.equals(arg2)) {
                            String hashedPassword = passwordHasher.hashPassword(arg1);

                            if(database.savePlayer(player.getName(), hashedPassword, PlayerGlobalRank.PLAYER, Instant.now())) {
                                system.setPlayerRegisterStatus(player.getName(), true, hashedPassword);

                                player.sendMessage(successfulUsage);
                                playerActivityStatus = PlayerActivityStatus.REGISTRATION;

                                //New login attempt
                                Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, LoginAttemptType.REGISTER));
                            } else
                                player.sendMessage(ChatColor.RED + "Server could not register you! Try a few minutes later.");
                        } else
                            player.sendMessage(ChatColor.RED + "Passwords are not equals!");
                    } else
                        player.sendMessage(ChatColor.RED + "Password needs to be long at least 5 chars!");
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
