package com.github.multidestroy.events.listeners;

import com.github.multidestroy.Config;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.PluginSystem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInteraction implements Listener {

    private final PluginSystem system;
    private final Config config;

    public PlayerInteraction(PluginSystem system, Config config) {
        this.system = system;
        this.config = config;
    }

    /**
     * Stop player from moving when not logged in.
     */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (config.get().getBoolean("settings.login_attempt.interaction.moving_blockade"))
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
        if (!config.get().getBoolean("settings.login_attempt.interaction.teleportation"))
            if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
                event.setCancelled(true);
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        PlayerInfo playerInfo = system.getPlayerInfo(event.getPlayer().getName());
        boolean allowBookReading = config.get().getBoolean("settings.login_attempt.interaction.items.allow_book_reading");
        boolean allowItemClick = config.get().getBoolean("settings.login_attempt.interaction.items.click");
        if (!playerInfo.isLoggedIn()) {
            if (allowBookReading)
                if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK))
                    if (event.getItem() != null && event.getItem().getType().equals(Material.WRITTEN_BOOK))
                        return;

            event.setCancelled(!allowItemClick);
        }
    }

    /**
     * Lock opening equipment when player is not logged in if this equipment is not player's equipment
     * Method to block for example: moving between servers by item which opens inventory with servers list
     */

    @EventHandler
    public void onOpeningForeignEquipment(InventoryOpenEvent event) {
        if (!config.get().getBoolean("settings.login_attempt.interaction.inventory.open_foreign")) {
            Inventory inv = event.getInventory();
            PlayerInventory playerInv = event.getPlayer().getInventory();
            if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
                if (!inv.getName().equals(playerInv.getName()))
                    event.setCancelled(true);
        }
    }


    /**
     * Prevent moving or throwing items from player's equipment when player is not logged in
     */

    @EventHandler
    public void onMovingItems(InventoryClickEvent event) {
        if (!config.get().getBoolean("settings.login_attempt.interaction.inventory.moving_items"))
            if (!system.isPlayerLoggedIn(event.getWhoClicked().getName()))
                event.setCancelled(true);
    }

    /**
     * Prevent picking items up when player is not logged in
     */

    @EventHandler
    public void onPickingItemsUp(EntityPickupItemEvent event) {
        if (!config.get().getBoolean("settings.login_attempt.interaction.items.picking"))
            if (event.getEntityType() == EntityType.PLAYER)
                if (!system.isPlayerLoggedIn(event.getEntity().getName()))
                    event.setCancelled(true);
    }

    /**
     * Disallow player to drop items from inventory when not logged in
     */

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!config.get().getBoolean("settings.login_attempt.interaction.items.dropping"))
            if (!system.isPlayerLoggedIn(event.getPlayer().getName()))
                event.setCancelled(true);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (!system.isPlayerLoggedIn(event.getDamager().getName()))
                event.setCancelled(!config.get().getBoolean("settings.login_attempt.interaction.entity_hit"));
        }
    }

    @EventHandler
    public void onVehicleHit(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player) {
            if (!system.isPlayerLoggedIn(event.getAttacker().getName()))
                event.setCancelled(!config.get().getBoolean("settings.login_attempt.interaction.vehicles.hit"));
        }
    }

    @EventHandler
    public void playerGetIntoVehicle(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            if (!system.isPlayerLoggedIn(event.getEntered().getName()))
                event.setCancelled(!config.get().getBoolean("settings.login_attempt.interaction.vehicles.enter"));
        }
    }

    private boolean isDifferentLocation(Location from, Location to) {
        if (from.getX() == to.getX())
            if (from.getY() == to.getY())
                return from.getZ() != to.getZ();
        return true;

    }

}
