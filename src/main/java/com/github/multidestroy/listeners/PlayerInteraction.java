package com.github.multidestroy.listeners;

import com.github.multidestroy.Config;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.PluginSystem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInteraction implements Listener {

    private PluginSystem system;
    private Config config;

    public PlayerInteraction(PluginSystem system, Config config) {
        this.system = system;
        this.config = config;
    }

    /**
     * Stop player from moving when not logged in.
     */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (config.get().getBoolean("settings.login_attempt.moving_blockade"))
            if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
                if (isDifferentLocation(event.getFrom(), event.getTo())) {
                    event.setTo(event.getFrom());
                    event.setCancelled(true);
                }
    }

    /**
     * Prevent any teleportation if player has not logged in
     */

    @EventHandler
    public void onTeleportation(PlayerTeleportEvent event) {
        if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        PlayerInfo playerInfo = system.getPlayerInfo(event.getPlayer().getName());
        boolean allowBookReading = config.get().getBoolean("settings.login_attempt.allow_book_reading");
        if(!playerInfo.isLoggedIn()) {
            if (allowBookReading)
                if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK))
                    if (event.getItem().getType().equals(Material.WRITTEN_BOOK))
                        return;

            event.setCancelled(true);
        }
    }

    /**
     * Lock opening equipment when player is not logged in if this equipment is not player's equipment
     * Method to block for example: moving between servers by item which opens inventory with servers list
     */

    @EventHandler
    public void onOpeningForeignEquipment(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        PlayerInventory playerInv = event.getPlayer().getInventory();
        if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
            if (!inv.getName().equals(playerInv.getName()))
                event.setCancelled(true);
    }

    /**
     * Prevent moving or throwing items from player's equipment when player is not logged in
     */

    @EventHandler
    public void onMovingItems(InventoryClickEvent event) {
        if(!system.isPlayerLoggedIn(event.getWhoClicked().getName()))
            event.setCancelled(true);
    }

    /**
     * Prevent picking items up when player is not logged in
     */

    @EventHandler
    public void onPickingItemsUp(EntityPickupItemEvent event) {
        if(event.getEntityType() == EntityType.PLAYER) {
            if(!system.isPlayerLoggedIn(event.getEntity().getName()))
                event.setCancelled(true);
        }
    }

    /**
     * Disallow player to drop items from inventory when not logged in
     */

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if(!system.isPlayerLoggedIn(event.getPlayer().getName()))
            event.setCancelled(true);
    }

    private boolean isDifferentLocation(Location from, Location to) {
        if(from.getX() == to.getX())
            if(from.getY() == to.getY())
                return from.getZ() != to.getZ();
        return true;

    }
}
