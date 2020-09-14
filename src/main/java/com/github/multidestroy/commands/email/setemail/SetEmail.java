package com.github.multidestroy.commands.email.setemail;

import com.github.multidestroy.configs.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerActivityStatus;
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

public class SetEmail implements CommandExecutor {

    private PluginSystem system;
    private Database database;
    private String successfulUsage;
    private JavaPlugin plugin;
    protected ThreadSystem threadSystem;
    public static String correctUsage = ChatColor.RED + "/setemail <new_e-mail> <new_e-mail>";

    public SetEmail(PluginSystem system, Database database, Config config, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.successfulUsage = config.getMessage("setemail");
        this.plugin = plugin;
        this.threadSystem = threadSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length == 2) {
                if (!system.isEmailAssigned(sender.getName())) {
                    if (PluginSystem.isEmailPossible(args[0])) {
                        if (args[0].equals(args[1])) {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, finalizeCommand((Player) sender, args[0]));
                        } else
                            sender.sendMessage(ChatColor.RED + "E-mails are not equal!");
                    } else
                        sender.sendMessage(ChatColor.RED + "New e-mail is not correct!");
                } else
                    sender.sendMessage(ChatColor.RED + "E-mail is already assigned to your account! Use /changeemail command instead.");
            } else
                sender.sendMessage(correctUsage);
        } else
            sender.sendMessage(ChatColor.RED + "Command is not available to use by the console!");
        return false;
    }

    protected Runnable finalizeCommand(Player player, String email) {
        return () -> {
            PlayerActivityStatus playerActivityStatus = null;
            if (threadSystem.tryLock(player.getName())) {
                try {
                    switch (database.changeEmail(player.getName(), email)) {
                        case -1:
                            //if e-mail could not be updated in database
                            player.sendMessage(ChatColor.RED + "E-mail could not be saved! Try a few minutes later.");
                        case 0:
                            //if e-mail is already assigned
                            player.sendMessage(ChatColor.RED + "E-mail is already assigned to the other account!");
                        case 1:
                            //if e-mail was successfully saved in the database
                            system.getPlayerInfo(player.getName()).setEmail(email);
                            playerActivityStatus = PlayerActivityStatus.EMAIL_CHANGE;
                            player.sendMessage(successfulUsage);
                    }
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
