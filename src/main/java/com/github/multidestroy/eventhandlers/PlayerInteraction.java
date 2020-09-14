package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.PluginSystem;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerInteraction implements Listener {

    private PluginSystem system;
    private boolean movingBlockade;

    public PlayerInteraction(PluginSystem system, boolean movingBlockade) {
        this.system = system;
        this.movingBlockade = movingBlockade;
    }

    /**
     * Stop player from moving when not logged in.
     */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (movingBlockade)
            if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
                if (isDifferentLocation(event.getFrom(), event.getTo()))
                    event.setTo(event.getFrom());
    }

    /**
     * Prevent any teleportation if player has not logged in
     */

    @EventHandler
    public void onTeleportation(PlayerTeleportEvent event) {
        if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
            event.setCancelled(true);
    }

    public void onItemClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        PlayerInfo playerInfo = system.getPlayerInfo(event.getPlayer().getName());

        if(!playerInfo.isLoggedIn())
            event.setCancelled(true);
    }

    private boolean isDifferentLocation(Location from, Location to) {
        return !from.equals(to);
    }
}
