package com.github.multidestroy.commands.email.setemail;

import com.github.multidestroy.Messages;
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
    private JavaPlugin plugin;
    protected ThreadSystem threadSystem;

    public SetEmail(PluginSystem system, Database database, Config config, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
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
                            sender.sendMessage(Messages.getColoredString("COMMAND.EMAIL.NOT_EQUAL"));
                    } else
                        sender.sendMessage(Messages.getColoredString("COMMAND.EMAIL.NEW.WRONG"));
                } else
                    sender.sendMessage(Messages.getColoredString("COMMAND.SETEMAIL.ALREADY_ASSIGNED"));
            } else
                sender.sendMessage(Messages.getColoredString("COMMAND.SETEMAIL.CORRECT_USAGE"));
        } else
            sender.sendMessage(Messages.getColoredString("COMMAND.CONSOLE.LOCK"));
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
                            player.sendMessage(Messages.getColoredString("ERROR"));
                        case 0:
                            //if e-mail is already assigned
                            player.sendMessage(Messages.getColoredString("EMAIL.ASSIGNED.OTHER"));
                        case 1:
                            //if e-mail was successfully saved in the database
                            system.getPlayerInfo(player.getName()).setEmail(email);
                            playerActivityStatus = PlayerActivityStatus.EMAIL_CHANGE;
                            player.sendMessage(Messages.getColoredString("COMMAND.SETEMAIL.SUCCESS"));
                    }
                } finally {
                    threadSystem.unlock(player.getName());
                }
            } else
                player.sendMessage(Messages.getColoredString("COMMAND.THREAD.LOCK"));

            if(playerActivityStatus != null)
                database.saveLoginAttempt(player, playerActivityStatus, Instant.now());
        };
    }
}
