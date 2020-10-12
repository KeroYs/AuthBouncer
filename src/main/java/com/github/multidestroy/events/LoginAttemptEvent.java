package com.github.multidestroy.events;

import com.github.multidestroy.Messages;
import com.github.multidestroy.player.PlayerInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.potion.PotionEffectType;

public class LoginAttemptEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private LoginAttemptType loginAttemptType;

    public LoginAttemptEvent(Player player, LoginAttemptType loginAttemptType) {
        super(player);
        this.isCancelled = false;
        this.loginAttemptType = loginAttemptType;
    }

    public void disallow() {
        if(player.isOnline())
            player.kickPlayer(Messages.getColoredString("LOGIN_ATTEMPT.DISALLOW"));
        isCancelled = true;
    }

    public LoginAttemptType getType() {
        return loginAttemptType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static void endLoginAttempt(Player player, PlayerInfo playerInfo) {
        if(playerInfo.isLoggedIn()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            player.setLevel(0);
            player.setExp(0);
            player.sendTitle("", "", 0, 0, 0);
        }
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
