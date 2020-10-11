package com.github.multidestroy.commands.email.changeemail;

import com.github.multidestroy.Messages;
import com.github.multidestroy.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerActivityStatus;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;

public class ChangeEmail implements CommandExecutor {

    private PluginSystem system;
    private Database database;
    protected ThreadSystem threadSystem;
    private JavaPlugin plugin;
    private Config config;

    public ChangeEmail(PluginSystem system, Database database, Config config, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.threadSystem = threadSystem;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length == 3) {
                if (system.isEmailAssigned(sender.getName())) {
                    if (system.isEmailCorrect(sender.getName(), args[0])) {
                        if (PluginSystem.isEmailPossible(args[1])) {
                            if (args[1].equals(args[2])) {
                                if(!args[0].equals(args[1]))
                                    Bukkit.getScheduler().runTaskAsynchronously(plugin, finalizeCommand((Player) sender, args[1]));
                                else
                                    sender.sendMessage(Messages.getColoredString("COMMAND.CHANGEEMAIL.SAME"));
                            } else
                                sender.sendMessage(Messages.getColoredString("COMMAND.EMAIL.NOT_EQUAL"));
                        } else
                            sender.sendMessage(Messages.getColoredString("COMMAND.EMAIL.NEW.WRONG"));
                    } else {
                        sender.sendMessage(Messages.getColoredString("COMMAND.CHANGEEMAIL.CURRENT.WRONG"));
                        if(config.get().getBoolean("settings.e-mail.hint"))
                            sender.sendMessage(Messages.getColoredString("EMAIL.HINT") + " " + system.emailHint(sender.getName()));
                    }
                } else
                    sender.sendMessage(Messages.getColoredString("COMMAND.CHANGEEMAIL.NOT_ASSIGNED"));
            } else
                sender.sendMessage(Messages.getColoredString("COMMAND.CHANGEEMAIL.CORRECT_USAGE"));
        } else
            sender.sendMessage(Messages.getColoredString("COMMAND.CONSOLE.LOCK"));
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
                            player.sendMessage(Messages.getColoredString("ERROR"));
                        case 0:
                            //if e-mail is already assigned
                            player.sendMessage(Messages.getColoredString("EMAIL.ASSIGNED.OTHER"));
                        case 1:
                            //if e-mail was successfully saved in the database
                            system.getPlayerInfo(player.getName()).setEmail(email);
                            playerActivityStatus = PlayerActivityStatus.EMAIL_CHANGE;
                            player.sendMessage(Messages.getColoredString("COMMAND.CHANGEEMAIL.SUCCESS"));
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
