package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.Utils;
import com.github.multidestroy.commands.Login;
import com.github.multidestroy.commands.Register;
import com.github.multidestroy.system.PluginSystem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class OnChat implements Listener {

    private PluginSystem pluginSystem;

    public OnChat(PluginSystem pluginSystem) {
        this.pluginSystem = pluginSystem;
    }

    /**
     * Disallow player to write anything on the chat, or to use any unpredicted command (only /register or /login)
     * when he is not registered and logged in.
     */

    @EventHandler
    public void OnPlayerChat(AsyncPlayerChatEvent event) {
        chatRestriction(event, event.getPlayer(), event.getMessage());
    }

    @EventHandler
    public void OnPlayerCommand(PlayerCommandPreprocessEvent event) {
        chatRestriction(event, event.getPlayer(), event.getMessage());
    }

    private void chatRestriction(Cancellable event, Player player, String message) {
        if (!pluginSystem.isPlayerRegistered(player.getName())) {
            if (!Utils.isCommand(message,"/register")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only use /register command!");
            }
        } else if (!pluginSystem.isPlayerLoggedIn(player.getName())) {
            if (!Utils.isCommand(message,"/login")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only use /login command!");
            }
        }
    }
}
