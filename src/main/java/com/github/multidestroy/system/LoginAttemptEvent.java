package com.github.multidestroy.system;

import com.github.multidestroy.eventhandlers.LoginSession;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LoginAttemptEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;

    public LoginAttemptEvent(Player player) {
        super(player);
        this.isCancelled = false;
    }

    public void disallow() {
        if(player.isOnline())
            player.kickPlayer(ChatColor.RED + "Time to log in is over!");
    }

    public void notifyAboutLoginSessionAccessibility() {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, LoginSession.sessionHint);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
