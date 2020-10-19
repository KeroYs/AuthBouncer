package com.github.multidestroy.bukkit.events.listeners;

import com.github.multidestroy.bukkit.player.PlayerInfo;
import com.github.multidestroy.bukkit.system.PluginSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This listener should be enabled only if server is not working with a BungeeCord
 */

public class OnLeave implements Listener {

    private final PluginSystem system;

    public OnLeave(PluginSystem system) {
        this.system = system;
    }

    @EventHandler
    public void playerLeftServer(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
        if (!system.isLoginSession(player.getName(), player.getAddress().getAddress()))
            playerInfo.setLoginStatus(false);
    }
}
