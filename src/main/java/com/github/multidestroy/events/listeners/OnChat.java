package com.github.multidestroy.events.listeners;

import com.github.multidestroy.Messages;
import com.github.multidestroy.Utils;
import com.github.multidestroy.system.PluginSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OnChat implements Listener {

    private PluginSystem pluginSystem;
    private JavaPlugin plugin;

    public OnChat(PluginSystem pluginSystem, JavaPlugin plugin) {
        this.pluginSystem = pluginSystem;
        this.plugin = plugin;
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
            if (!Utils.isCommand(message,"register", plugin)) {
                event.setCancelled(true);
                player.sendMessage(Messages.getColoredString("CHAT_RESTRICTION.REGISTER"));
            }
        } else if (!pluginSystem.isPlayerLoggedIn(player.getName())) {
            if (!Utils.isCommand(message,"login", plugin)) {
                event.setCancelled(true);
                player.sendMessage(Messages.getColoredString("CHAT_RESTRICTION.LOGIN"));
            }
        }
    }
}
