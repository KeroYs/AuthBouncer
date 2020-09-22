package com.github.multidestroy.commands;

import com.github.multidestroy.Messages;
import com.github.multidestroy.PasswordHasher;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerActivityStatus;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.Bukkit;
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
    private Config config;
    private JavaPlugin plugin;
    private ThreadSystem threadSystem;

    public ChangePassword(PluginSystem system, Database database, Config config, PasswordHasher passwordHasher, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.config = config;
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
                sender.sendMessage(Messages.getColoredString("COMMAND.CHANGEPASSWORD.CORRECT_USAGE"));
        } else
            sender.sendMessage(Messages.getColoredString("COMMAND.CONSOLE.LOCK"));
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
                                    player.sendMessage(Messages.getColoredString("COMMAND.CHANGEPASSWORD.SUCCESS"));
                                    playerActivityStatus = PlayerActivityStatus.PASSWORD_CHANGE;
                                } else
                                    player.sendMessage(Messages.getColoredString("ERROR"));
                            } else
                                player.sendMessage(Messages.getColoredString("COMMAND.PASSWORD.NOT_EQUAL"));
                        } else
                            player.sendMessage(Messages.getColoredString("COMMAND.REGISTER.PASSWORD.RESTRICTION"));
                    } else
                        player.sendMessage(Messages.getColoredString("COMMAND.CHANGEPASSWORD.CURRENT.WRONG"));
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
