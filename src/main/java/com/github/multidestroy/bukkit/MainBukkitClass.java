package com.github.multidestroy.bukkit;

import com.github.multidestroy.bukkit.commands.ChangePassword;
import com.github.multidestroy.bukkit.commands.Login;
import com.github.multidestroy.bukkit.commands.Register;
import com.github.multidestroy.bukkit.commands.email.changeemail.ChangeEmail;
import com.github.multidestroy.bukkit.commands.email.setemail.SetEmail;
import com.github.multidestroy.bukkit.database.Database;
import com.github.multidestroy.bukkit.events.LoginAttemptEvent;
import com.github.multidestroy.bukkit.events.listeners.*;
import com.github.multidestroy.bukkit.i18n.Messages;
import com.github.multidestroy.bukkit.player.PlayerInfo;
import com.github.multidestroy.bukkit.system.PluginSystem;
import com.github.multidestroy.bukkit.system.ThreadSystem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MainBukkitClass extends JavaPlugin {

    private Config config;
    private Database database;
    private PasswordHasher passwordHasher;
    private PluginSystem system;
    private ThreadSystem passwordThreadSystem;
    private ThreadSystem emailThreadSystem;
    private ChannelMessenger channelMessage;
    private CommandsFilter commandsFilter;

    @Override
    public void onEnable() {
        database = null;

        /*              Config              */
        config = new Config("config.yml");
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
        channelMessage = new ChannelMessenger(system, passwordThreadSystem, this, config);
        if (channelMessage.checkIfSpigot()) {
            if (!channelMessage.checkIfBungee(this)) {
                if (database.reloadDataSource()) {
                    database.saveDefaultTables();

                    //Register plugin message channel, commands and events
                    if (config.get().getBoolean("settings.bungeecord")) {
                        getServer().getMessenger().registerIncomingPluginChannel(this, "authbouncer:channel", channelMessage);
                        getServer().getMessenger().registerOutgoingPluginChannel(this, "authbouncer:channel");
                    }
                    registerCommands();
                    registerEvents();

                    //Register logger filter
                    commandsFilter = new CommandsFilter(this);

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

        commandsFilter.stopFilter();
    }

    private void registerCommands() {
        getCommand("register").setExecutor(new Register(system, database, config, passwordHasher, passwordThreadSystem, channelMessage, this));
        getCommand("login").setExecutor(new Login(system, database, config, passwordThreadSystem, this, channelMessage));
        getCommand("changepassword").setExecutor(new ChangePassword(system, database, config, passwordHasher, passwordThreadSystem, this));

//        Email authorization does not work int this version
//        if (settings.email_authorization) {
//
//            /*             E-mail              */
//            EmailConfig emailConfig = new EmailConfig();
//            emailConfig.setup(getDataFolder(), this);
//            emailConfig.save();
//            EmailSender emailSender = new EmailSender();
//            getCommand("setemail").setExecutor(new SetEmail_Authorization(system, database, config, emailSender, emailThreadSystem, this));
//            getCommand("changeemail").setExecutor(new ChangeEmail_Authorization(system, database, config, emailSender, emailThreadSystem, this));
//            /*                                 */
//

        //} else {
        getCommand("setemail").setExecutor(new SetEmail(system, database, config, emailThreadSystem, this));
        getCommand("changeemail").setExecutor(new ChangeEmail(system, database, config, emailThreadSystem, this));
        //}
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new OnJoin(system, database, config, channelMessage), this);
        getServer().getPluginManager().registerEvents(new OnChat(system, this), this);
        getServer().getPluginManager().registerEvents(new LoginAttempt(system, this, passwordThreadSystem, config), this);
        getServer().getPluginManager().registerEvents(new PlayerInteraction(system, config), this);
        if (config.get().getBoolean("settings.login_session.allow"))
            getServer().getPluginManager().registerEvents(new LoginSession(system), this);
        if (!config.get().getBoolean("settings.bungeecord"))
            getServer().getPluginManager().registerEvents(new OnLeave(system), this);
    }

    private void forcePlayersToLogIn() {
        getServer().getOnlinePlayers().forEach(player -> {
            PlayerInfo playerInfo;
            if ((playerInfo = database.getRegisteredPlayer(player.getName())) != null)
                system.saveNewPlayer(player.getName(), playerInfo); //player is already registered
            else
                system.saveNewPlayer(player.getName(), playerInfo = new PlayerInfo()); //player has never registered

            if (config.get().getBoolean("settings.bungeecord"))
                channelMessage.sendLoginStatusChangeMessage(player, false);
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, playerInfo, config, channelMessage, false));
        });
    }
}
