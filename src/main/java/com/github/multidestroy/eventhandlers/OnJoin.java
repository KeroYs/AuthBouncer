package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.Messages;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
import com.github.multidestroy.system.PluginSystem;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.w3c.dom.Attr;

public class OnJoin implements Listener {

    private PluginSystem system;
    private Database database;
    private Config config;

    public OnJoin(PluginSystem system, Database database, Config config) {
        this.system = system;
        this.database = database;
        this.config = config;
    }

    /**
     * During pre-login event, player is saving into the system
     */

    @EventHandler
    public void OnPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        if(database.isConnected()) {
            if (!system.isPlayerInSystem(playerName)) { //Check if player was ever saved into the system
                PlayerInfo playerFromDatabase;
                if ((playerFromDatabase = database.getRegisteredPlayer(playerName)) != null)
                    system.saveNewPlayer(playerName, playerFromDatabase); //player is already registered
                else
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getColoredString("ERROR"));
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
    public void OnPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
        boolean isLoginSessionAvailable = config.get().getBoolean("settings.session");
        //set player's GameMode to survival
        if (player.getGameMode() != GameMode.SURVIVAL)
            player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        //Check if login session is enabled in the settings
        if (isLoginSessionAvailable && system.isLoginSession(player.getName(), event.getPlayer().getAddress().getAddress())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Messages.getColoredString("SESSION.ENABLE")));
            system.getPlayerInfo(player.getName()).setLoginStatus(true);
        } else { //Player must log in to the server
            LoginAttemptType loginAttemptType = playerInfo.isRegistered() ? LoginAttemptType.LOGIN : LoginAttemptType.REGISTER;
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, loginAttemptType));
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
