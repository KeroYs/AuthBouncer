package com.github.multidestroy;

import com.github.multidestroy.commands.*;
import com.github.multidestroy.commands.ChangePassword;
import com.github.multidestroy.commands.email.changeemail.ChangeEmail;
import com.github.multidestroy.commands.email.changeemail.ChangeEmail_Authorization;
import com.github.multidestroy.commands.email.setemail.SetEmail;
import com.github.multidestroy.commands.email.setemail.SetEmail_Authorization;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.eventhandlers.*;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private Config config;
    private Database database;
    private PasswordHasher passwordHasher;
    private PluginSystem system;
    private ThreadSystem passwordThreadSystem;
    private ThreadSystem emailThreadSystem;

    @Override
    public void onEnable() {
        Instant start = Instant.now();
        database = null;

        /*              Config              */
        config = new Config();
        config.setup(this);

        //Select resource bundle
        Messages.loadTranslation(config);
        getLogger().info(Messages.getColoredString("LANGUAGE"));

        database = new Database(config);
        passwordHasher = new PasswordHasher(config);
        system = new PluginSystem(passwordHasher);
        passwordThreadSystem = new ThreadSystem();
        emailThreadSystem = new ThreadSystem();

        /*      Plugin message channel with bungeecord      */
        ChannelMessageReceiver channelMessageReceiver = new ChannelMessageReceiver(system, passwordThreadSystem, this);
        if (channelMessageReceiver.checkIfSpigot()) {
            if (!channelMessageReceiver.checkIfBungee(this)) {

                if(database.reloadDataSource()) {
                    database.saveDefaultTables();

                    //Register plugin message channel
                    getServer().getMessenger().registerIncomingPluginChannel(this, ChannelMessageReceiver.globalChannel, channelMessageReceiver);
                    registerCommands();
                    registerEvents();

                    //Force new players to log in
                    forcePlayersToLogIn();
                    getLogger().info("Bouncer ON");
                    return;
                } else {
                    getLogger().severe(Messages.getColoredString("LOGGER.DATABASE.CONNECTION.FALSE"));
                    //Kick players from the servers
                    getServer().getOnlinePlayers().forEach(player -> player.kickPlayer(Messages.getColoredString("ERROR")));
                }

            } else {
                getLogger().severe(Messages.getColoredString("LOGGER.PLUGIN_MESSAGE_CHANNEL.NOT_BUNGEE"));
                getLogger().severe(Messages.getColoredString("LOGGER.DISABLE"));
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            getLogger().severe(Messages.getColoredString("LOGGER.PLUGIN_MESSAGE_CHANNEL.NOT_SPIGOT"));
            getLogger().severe(Messages.getColoredString("LOGGER.DISABLE"));
            getServer().getPluginManager().disablePlugin(this);
        }
        getLogger().severe("Bouncer OFF");
    }

    @Override
    public void onDisable() {
        if (database != null)
            database.close();
    }

    private void registerCommands () {
        getCommand("register").setExecutor(new Register(system, database, config, passwordHasher, passwordThreadSystem, this));
        getCommand("login").setExecutor(new Login(system, database, config, passwordThreadSystem, this));
        getCommand("changepassword").setExecutor(new ChangePassword(system, database, config, passwordHasher, passwordThreadSystem, this));

       // if (settings.email_authorization) {

            /*             E-mail              */
  /*          EmailConfig emailConfig = new EmailConfig();
            emailConfig.setup(getDataFolder(), this);
            emailConfig.save();
            EmailSender emailSender = new EmailSender();
            getCommand("setemail").setExecutor(new SetEmail_Authorization(system, database, config, emailSender, emailThreadSystem, this));
            getCommand("changeemail").setExecutor(new ChangeEmail_Authorization(system, database, config, emailSender, emailThreadSystem, this));
            /*                                 */

        //} else {
            getCommand("setemail").setExecutor(new SetEmail(system, database, config, emailThreadSystem, this));
            getCommand("changeemail").setExecutor(new ChangeEmail(system, database, config, emailThreadSystem, this));
        //}
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new OnJoin(system, database, config), this);
        getServer().getPluginManager().registerEvents(new OnChat(system, this), this);
        getServer().getPluginManager().registerEvents(new LoginAttempt(system,this, passwordThreadSystem, config), this);
        getServer().getPluginManager().registerEvents(new PlayerInteraction(system, config), this);
        if(config.get().getBoolean("settings.session"))
            getServer().getPluginManager().registerEvents(new LoginSession(system), this);
    }

    private void forcePlayersToLogIn() {
        getServer().getOnlinePlayers().forEach(player -> {
            PlayerInfo playerInfo;
            if ((playerInfo = database.getRegisteredPlayer(player.getName())) != null)
                system.saveNewPlayer(player.getName(), playerInfo); //player is already registered
            else
                system.saveNewPlayer(player.getName(), playerInfo = new PlayerInfo()); //player has never registered

            LoginAttemptType loginAttemptType = playerInfo.isRegistered() ? LoginAttemptType.LOGIN : LoginAttemptType.REGISTER;
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, loginAttemptType));
        });
    }
}
