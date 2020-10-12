package com.github.multidestroy.commands;

import com.github.multidestroy.Messages;
import com.github.multidestroy.PasswordHasher;
import com.github.multidestroy.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.events.listeners.LoginSession;
import com.github.multidestroy.player.PlayerActivityStatus;
import com.github.multidestroy.player.PlayerGlobalRank;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.events.LoginAttemptEvent;
import com.github.multidestroy.events.LoginAttemptType;
import com.github.multidestroy.system.PluginSystem;

import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;

public class Register implements CommandExecutor {

    private Database database;
    private PluginSystem system;
    private PasswordHasher passwordHasher;
    private Config config;
    private ThreadSystem threadSystem;
    private JavaPlugin plugin;

    public Register(PluginSystem system, Database database, Config config, PasswordHasher passwordHasher, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.passwordHasher = passwordHasher;
        this.config = config;
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
                    sender.sendMessage(Messages.getColoredString("COMMAND.REGISTER.CORRECT_USAGE"));
            } else
                sender.sendMessage(Messages.getColoredString("COMMAND.REGISTER.ALREADY_REGISTERED"));
        } else
            sender.sendMessage(Messages.getColoredString("COMMAND.CONSOLE.LOCK"));
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

                                player.sendMessage(Messages.getColoredString("COMMAND.REGISTER.SUCCESS"));
                                playerActivityStatus = PlayerActivityStatus.REGISTRATION;


                                if(config.get().getBoolean("settings.login_attempt.login_after_registration")) //Player has to log in after registration
                                    Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, LoginAttemptType.LOGIN));
                                else {
                                    playerInfo.setLoginStatus(true);
                                    LoginAttemptEvent.endLoginAttempt(player, playerInfo);
                                    playerInfo.setLastSuccessfulIp(player.getAddress().getAddress());
                                    if(config.get().getBoolean("settings.login_session.allow"))
                                        LoginSession.notifyAboutSessionAccessibility(player, playerInfo, plugin, config);
                                }
                            } else
                                player.sendMessage(Messages.getColoredString("ERROR"));
                        } else
                            player.sendMessage(Messages.getColoredString("COMMAND.PASSWORD.NOT_EQUAL"));
                    } else
                        player.sendMessage(Messages.getColoredString("COMMAND.REGISTER.PASSWORD.RESTRICTION"));
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
