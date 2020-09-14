package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.database.Database;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.PluginSystem;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class OnJoin implements Listener {

    private PluginSystem system;
    private Database database;
    private Settings settings;
    private static ItemStack helpBook;
    static {
        helpBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) helpBook.getItemMeta();
        bookMeta.setTitle(ChatColor.RED + "Help");
        bookMeta.setAuthor("MultiDestroy");
        bookMeta.setPages("There is a tutorial");
        helpBook.setItemMeta(bookMeta);
    }

    public OnJoin(PluginSystem system, Database database, Settings settings) {
        this.system = system;
        this.database = database;
        this.settings = settings;
    }

    /**
     * During pre-login event, player is saving into the system
     */

    @EventHandler
    public void OnPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        if (!system.isPlayerInSystem(playerName)) { //Check if player was ever saved into the system
            PlayerInfo playerFromDatabase;
            //TODO wyrzucenie
            if ((playerFromDatabase = database.getRegisteredPlayer(playerName)) != null)
                system.saveNewPlayer(playerName, playerFromDatabase); //player is already registered
            else
                system.saveNewPlayer(playerName, new PlayerInfo()); //player has never registered
        }
    }

    /**
     * When player has got through the pre login event, now has to be checked whether session is ON.
     * If login session if OFF, LoginAttempt event is called.
     * Player's GameMode is set to survival.
     */

    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.getInventory().contains(helpBook))
            if(player.getInventory().getItem(9) == null)
                player.getInventory().setItem(9, helpBook); //To config!!!

        //set player's GameMode to survival
        if (player.getGameMode() != GameMode.SURVIVAL)
            player.setGameMode(GameMode.SURVIVAL);
        //Check if login session is enabled in the settings
        if (settings.session && system.isLoginSession(player.getName(), event.getPlayer().getAddress().getAddress())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, LoginSession.sessionON);
            system.getPlayerInfo(player.getName()).setLoginStatus(true);
        } else //Player must log in to the server
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(event.getPlayer()));

    }


    /**
     * If Player missed pre-login event kick him from the server
     */

    @EventHandler
    public void missedPreLoginEvent(PlayerLoginEvent event) {
        if (!system.isPlayerInSystem(event.getPlayer().getName()))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Join again!");
    }
}
