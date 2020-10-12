package com.github.multidestroy.listeners;

import com.github.multidestroy.Messages;
import com.github.multidestroy.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.time.Instant;

public class OnJoin implements Listener {

    private PluginSystem system;
    private Database database;
    private Config config;
    private JavaPlugin plugin;
    private ThreadSystem passwordThreadSystem;

    public OnJoin(PluginSystem system, Database database, Config config, JavaPlugin plugin, ThreadSystem passwordThreadSystem) {
        this.system = system;
        this.database = database;
        this.config = config;
        this.plugin = plugin;
        this.passwordThreadSystem = passwordThreadSystem;
    }

    /**
     * During pre-login event, player is saving into the system
     */

    @EventHandler
    public void OnPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        if(database.isConnected()) {

            System.out.println(event.getAddress().getHostAddress());
            switch (database.checkIpBlockade(event.getName(), event.getAddress())) {
                case -1:
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getColoredString("ERROR"));
                    break;
                case 0:
                    if (!system.isPlayerInSystem(playerName)) { //Check if player was ever saved into the system
                        PlayerInfo playerFromDatabase;
                        if ((playerFromDatabase = database.getRegisteredPlayer(playerName)) != null)
                            system.saveNewPlayer(playerName, playerFromDatabase); //player is already registered
                        else
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getColoredString("ERROR"));
                    }
                    break;
                case 1:
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getColoredString("IP_BLOCKADE.JOIN"));
                    break;
            }


        } else
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getColoredString("ERROR"));
    }

    /**
     * When player has got through the pre login event, now has to be checked whether session is ON.
     * If login session if OFF, LoginAttempt event is called.
     * Player's GameMode is set to survival.
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
        playerInfo.setLoginStatus(false);

        boolean isLoginSessionAvailable = config.get().getBoolean("settings.login_session");
        // Set player's food, health and GameMode according to plugin settings
        if(!isLoginSessionAvailable || !playerInfo.isLoginSession()) {
            if(config.get().getBoolean("settings.join.max_hunger"))
                player.setFoodLevel(20);
            if(config.get().getBoolean("settings.join.max_health"))
                player.setHealth(20);
            if(config.get().getBoolean("settings.join.gamemode.enforce"))
                player.setGameMode(GameMode.valueOf(config.get().getString("settings.join.gamemode.default")));
        }

    }

    /**
     * If Player missed pre-login event kick him from the server
     */

    @EventHandler
    public void missedPreLoginEvent(PlayerLoginEvent event) {
        if (!system.isPlayerInSystem(event.getPlayer().getName()))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Messages.getColoredString("EVENT.PRELOGIN.MISSED"));
    }

}
