package com.github.multidestroy.commands;

import com.github.multidestroy.configs.Config;
import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerActivityStatus;
import com.github.multidestroy.player.PlayerInfo;
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

public class Login implements CommandExecutor {

    private PluginSystem system;
    private Database database;
    private String successfulUsage;
    private Settings settings;
    private JavaPlugin plugin;
    private ThreadSystem threadSystem;
    public static String correctUsage = ChatColor.RED + "/login <password>";

    public Login(PluginSystem system, Database database, Config config, Settings settings, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.system = system;
        this.database = database;
        this.successfulUsage = config.getMessage("login");
        this.settings = settings;
        this.plugin = plugin;
        this.threadSystem = threadSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!system.isPlayerLoggedIn(sender.getName())) {
                if (args.length == 1) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncTask((Player) sender, args[0]));
                } else
                    sender.sendMessage(correctUsage);
            } else
                sender.sendMessage(ChatColor.RED + "You are already logged in!");
        } else
            sender.sendMessage(ChatColor.RED + "Command is not available to use by the console!");

        return false;
    }

    private Runnable asyncTask(Player player, String password) {
        return () -> {
            PlayerActivityStatus playerActivityStatus = PlayerActivityStatus.UNSUCCESSFUL_LOGIN;
            PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
            if (threadSystem.tryLock(player.getName())) {
                try {
                    if (PluginSystem.isPasswordPossible(password)) {
                        if (system.isPasswordCorrect(playerInfo, password)) {
                            if (player.isOnline()) {
                                playerInfo.setLoginStatus(true);
                                playerInfo.setLastSuccessfulIp(player.getAddress().getAddress());
                                playerInfo.resetBlockadeCounter();
                                playerActivityStatus = PlayerActivityStatus.SUCCESSFUL_LOGIN;

                                player.sendMessage(successfulUsage);
                            }
                        } else if (!lowerBlockadeCounter(playerInfo, player))
                            player.sendMessage(ChatColor.RED + "Wrong password!");
                    } else if (!lowerBlockadeCounter(playerInfo, player))
                        player.sendMessage(ChatColor.RED + "Wrong password!");
                } finally {
                    threadSystem.unlock(player.getName());
                }
            } else
                player.sendMessage(ChatColor.RED + "Wait until previous command is done!");

            database.saveLoginAttempt(player, playerActivityStatus, Instant.now());
        };
    }

    /**
     * @return TRUE - if player was blockaded, otherwise FALSE
     */

    private boolean lowerBlockadeCounter(PlayerInfo playerInfo, Player player) {
        boolean returnValue;
        if (returnValue = playerInfo.lowerBlockadeCounter(settings, player.getAddress().getAddress())) {
            database.lockIpAddressOnAccount(player.getAddress().getAddress(), player.getName(), Instant.now());
            player.kickPlayer(ChatColor.DARK_RED + "Your IP address on that account has been blocked! You will not be able to log in anymore.\n" +
                    "You can unlock your account through the website.");
        }
        return returnValue;
    }

}
