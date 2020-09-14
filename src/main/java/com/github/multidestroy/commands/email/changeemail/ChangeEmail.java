package com.github.multidestroy.commands.email.changeemail;

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

public class ChangeEmail implements CommandExecutor {

    private PluginSystem system;
    private Database database;
    private String successfulUsage;
    protected ThreadSystem threadSystem;
    private JavaPlugin plugin;
    public static String correctUsage = ChatColor.RED + "/changeemail <old_e-mail> <new_e-mail> <new_e-mail>";

    public ChangeEmail(PluginSystem system, Database database, Config config, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.threadSystem = threadSystem;
        this.plugin = plugin;
        this.successfulUsage = config.getMessage("changeemail");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length == 3) {
                if (system.isEmailAssigned(sender.getName())) {
                    if (system.isEmailCorrect(sender.getName(), args[0])) {
                        if (PluginSystem.isEmailPossible(args[1])) {
                            if (args[1].equals(args[2])) {
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, finalizeCommand((Player) sender, args[1]));
                            } else
                                sender.sendMessage(ChatColor.RED + "E-mails are not equal!");
                        } else
                            sender.sendMessage(ChatColor.RED + "New e-mail is not correct!");
                    } else
                        sender.sendMessage(ChatColor.RED + "Current e-mail is not correct\nHint: " + system.emailHint(sender.getName()));
                } else
                    sender.sendMessage(ChatColor.RED + "E-mail is not assigned to your account! Use /setemail command instead.");
            } else
                sender.sendMessage(correctUsage);
        } else
            sender.sendMessage(ChatColor.RED + "Command is not available to use by the console!");
        return false;
    }

    /**
     * Last step in executing command, when every requirements has been met
     * @return true - if command was executed properly; false - if command was not executed properly
     */

    protected Runnable finalizeCommand(Player player, String email) {
        return () -> {
            PlayerActivityStatus playerActivityStatus = null;
            if (threadSystem.tryLock(player.getName())) {
                try {
                    switch (database.changeEmail(player.getName(), email)) {
                        case -1:
                            //if e-mail could not be updated in database
                            player.sendMessage(ChatColor.RED + "E-mail could not be changed! Try a few minutes later.");
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
