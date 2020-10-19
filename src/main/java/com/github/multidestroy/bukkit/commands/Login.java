package com.github.multidestroy.bukkit.commands;

import com.github.multidestroy.bukkit.ChannelMessenger;
import com.github.multidestroy.bukkit.Config;
import com.github.multidestroy.bukkit.i18n.Messages;
import com.github.multidestroy.bukkit.database.Database;
import com.github.multidestroy.bukkit.events.LoginAttemptEvent;
import com.github.multidestroy.bukkit.events.listeners.LoginSession;
import com.github.multidestroy.bukkit.player.PlayerActivityStatus;
import com.github.multidestroy.bukkit.player.PlayerInfo;
import com.github.multidestroy.bukkit.system.PluginSystem;
import com.github.multidestroy.bukkit.system.ThreadSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;

public class Login implements CommandExecutor {

    private final PluginSystem system;
    private final Database database;
    private final Config config;
    private final JavaPlugin plugin;
    private final ThreadSystem threadSystem;
    private final ChannelMessenger channelMessage;

    public Login(PluginSystem system, Database database, Config config, ThreadSystem threadSystem, JavaPlugin plugin, ChannelMessenger channelMessage) {
        this.system = system;
        this.database = database;
        this.config = config;
        this.plugin = plugin;
        this.threadSystem = threadSystem;
        this.channelMessage = channelMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!system.isPlayerLoggedIn(sender.getName())) {
                if (args.length == 1) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncTask((Player) sender, args[0]));
                } else
                    sender.sendMessage(Messages.getColoredString("COMMAND.LOGIN.CORRECT_USAGE"));
            } else
                sender.sendMessage(Messages.getColoredString("COMMAND.LOGIN.ALREADY_LOGGED"));
        } else
            sender.sendMessage(Messages.getColoredString("COMMAND.CONSOLE.LOCK"));

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
                                LoginAttemptEvent.endLoginAttempt(player, playerInfo);
                                playerActivityStatus = PlayerActivityStatus.SUCCESSFUL_LOGIN;

                                player.sendMessage(Messages.getColoredString("COMMAND.LOGIN.SUCCESS"));
                                channelMessage.sendLoginStatusChangeMessage(player, true);
                                if (config.get().getBoolean("settings.login_session.allow"))
                                    LoginSession.notifyAboutSessionAccessibility(player, playerInfo, plugin, config);
                            }
                        } else if (!lowerBlockadeCounter(playerInfo, player))
                            player.sendMessage(Messages.getColoredString("COMMAND.PASSWORD.WRONG"));
                    } else if (!lowerBlockadeCounter(playerInfo, player))
                        player.sendMessage(Messages.getColoredString("COMMAND.PASSWORD.WRONG"));
                } finally {
                    threadSystem.unlock(player.getName());
                }
            } else
                player.sendMessage(Messages.getColoredString("COMMAND.THREAD.LOCK"));

            database.saveLoginAttempt(player, playerActivityStatus, Instant.now());
        };
    }

    /**
     * @return TRUE - if player was blockaded, otherwise FALSE
     */

    private boolean lowerBlockadeCounter(PlayerInfo playerInfo, Player player) {
        boolean returnValue;
        if (returnValue = playerInfo.lowerBlockadeCounter(config, player.getAddress().getAddress())) {
            if (database.lockIpAddressOnAccount(player.getAddress().getAddress(), player.getName(), Instant.now())) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.kickPlayer(Messages.getColoredString("IP_BLOCKADE.BLOCKADE"));
                    }
                }.runTask(plugin);
            } else
                player.kickPlayer(Messages.getColoredString("ERROR"));
        }
        return returnValue;
    }

}
