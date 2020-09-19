package com.github.multidestroy;

import com.github.multidestroy.commands.*;
import com.github.multidestroy.commands.ChangePassword;
import com.github.multidestroy.commands.email.changeemail.ChangeEmail;
import com.github.multidestroy.commands.email.changeemail.ChangeEmail_Authorization;
import com.github.multidestroy.commands.email.setemail.SetEmail;
import com.github.multidestroy.commands.email.setemail.SetEmail_Authorization;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.configs.EmailConfig;
import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.eventhandlers.*;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;

public class Main extends JavaPlugin {

    private Config config;
    private Database database;
    private Settings settings;
    private PasswordHasher passwordHasher;
    private PluginSystem system;
    private ThreadSystem passwordThreadSystem;
    private ThreadSystem emailThreadSystem;

    @Override
    public void onEnable() {
        Instant start = Instant.now();

        /*              Config              */
        config = new Config();
        config.setup(getDataFolder(), this);
        config.save();

        database = new Database(config);
        settings = new Settings(config);
        passwordHasher = new PasswordHasher();
        system = new PluginSystem(passwordHasher);
        passwordThreadSystem = new ThreadSystem();
        //emailThreadSystem = new ThreadSystem();

        /*      Plugin message channel with bungeecord      */
        ChannelMessageReceiver channelMessageReceiver = new ChannelMessageReceiver(system, passwordThreadSystem, this);
        channelMessageReceiver.checkIfBungee(this);
        getServer().getMessenger().registerIncomingPluginChannel(this, ChannelMessageReceiver.globalChannel, channelMessageReceiver);
        
        registerCommands();
        registerEvents();

        //Force new players to log in
        getServer().getOnlinePlayers().forEach(player -> {
            PlayerInfo playerInfo;
            if ((playerInfo = database.getRegisteredPlayer(player.getName())) != null)
                system.saveNewPlayer(player.getName(), playerInfo); //player is already registered
            else
                system.saveNewPlayer(player.getName(), playerInfo = new PlayerInfo()); //player has never registered

            LoginAttemptType loginAttemptType = playerInfo.isRegistered() ? LoginAttemptType.LOGIN : LoginAttemptType.REGISTER;
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, loginAttemptType));
        });

        pluginStatusInfoText(settings);
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Bouncer] " + ChatColor.GREEN + "Launched in: " + ((float) (Instant.now().toEpochMilli() - start.toEpochMilli()))/1000 + " s");
    }

    private void registerCommands() {
        getCommand("register").setExecutor(new Register(system, database, config, passwordHasher, settings, passwordThreadSystem, this));
        getCommand("login").setExecutor(new Login(system, database, config, settings, passwordThreadSystem, this));
        getCommand("changepassword").setExecutor(new ChangePassword(system, database, config, passwordHasher, passwordThreadSystem, this));

        if(settings.email_authorization) {

            /*             E-mail              */
            EmailConfig emailConfig = new EmailConfig();
            emailConfig.setup(getDataFolder(), this);
            emailConfig.save();
            EmailSender emailSender = new EmailSender();
            getCommand("setemail").setExecutor(new SetEmail_Authorization(system, database, config, emailSender, emailThreadSystem, this));
            getCommand("changeemail").setExecutor(new ChangeEmail_Authorization(system, database, config, emailSender, emailThreadSystem, this));
            /*                                 */

        } else {
            getCommand("setemail").setExecutor(new SetEmail(system, database, config, emailThreadSystem, this));
            getCommand("changeemail").setExecutor(new ChangeEmail(system, database, config, emailThreadSystem, this));
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new OnJoin(system, database,  settings), this);
        getServer().getPluginManager().registerEvents(new OnChat(system), this);
        getServer().getPluginManager().registerEvents(new LoginAttempt(settings, system,this, passwordThreadSystem, config), this);
        getServer().getPluginManager().registerEvents(new PlayerInteraction(system, settings.moving_blockade), this);
        if(settings.session)
            getServer().getPluginManager().registerEvents(new LoginSession(system, settings), this);
    }


    private void pluginStatusInfoText(Settings settings) {
        ConsoleCommandSender console = getServer().getConsoleSender();
        boolean tmp;
        console.sendMessage("|      " + ChatColor.GOLD + "Bouncer configuration" + ChatColor.WHITE + "      |");
        console.sendMessage("|   E-mail auth   |      " + ((tmp = settings.email_authorization) ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");
        if(tmp)
            console.sendMessage("|   E-mail hint   |      " + (settings.email_hint ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");

        console.sendMessage("|   Login time    |      " + ChatColor.YELLOW + settings.login_attempt_time + ChatColor.WHITE + "       |");
        console.sendMessage("|   Ip blockade   |      " + ((tmp = settings.ip_blockade) ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");
        if(tmp) {
            console.sendMessage("|   Same Ip low   |      " + ChatColor.YELLOW + settings.same_ip_blockade_low + ChatColor.WHITE + "       |");
            console.sendMessage("|   Same Ip top   |      " + ChatColor.YELLOW + settings.same_ip_blockade_top + ChatColor.WHITE + "       |");
            console.sendMessage("|   Diff Ip low   |      " + ChatColor.YELLOW + settings.different_ip_blockade_low + ChatColor.WHITE + "       |");
            console.sendMessage("|   Diff Ip top   |      " + ChatColor.YELLOW + settings.different_ip_blockade_top + ChatColor.WHITE + "       |");
        }
        console.sendMessage("|    Blindness    |      " + (settings.blindness ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");
        console.sendMessage("|    Slowness     |      " + (settings.slowness ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");
        console.sendMessage("| Moving Blockade |      " + (settings.moving_blockade ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");
        console.sendMessage("|     Session     |      " + (settings.session ? ChatColor.GREEN + "ON " : ChatColor.RED + "OFF") + ChatColor.WHITE + "      |");
    }
}
