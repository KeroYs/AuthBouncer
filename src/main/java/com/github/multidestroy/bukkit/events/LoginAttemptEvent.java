package com.github.multidestroy.bukkit.events;

import com.github.multidestroy.bukkit.ChannelMessenger;
import com.github.multidestroy.bukkit.Config;
import com.github.multidestroy.bukkit.events.listeners.LoginSession;
import com.github.multidestroy.bukkit.i18n.Messages;
import com.github.multidestroy.bukkit.player.PlayerInfo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.potion.PotionEffectType;

public class LoginAttemptEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private PlayerInfo playerInfo;
    private final LoginAttemptType loginAttemptType;

    public LoginAttemptEvent(Player player, PlayerInfo playerInfo, Config config, ChannelMessenger channelMessenger, boolean loginStatusAtBeginning) {
        super(player);
        this.isCancelled = false;
        this.playerInfo = playerInfo;
        loginAttemptType = playerInfo.isRegistered() ? LoginAttemptType.LOGIN : LoginAttemptType.REGISTER;

        startLoginAttempt(config, channelMessenger, loginStatusAtBeginning);
    }

    public void disallow() {
        if (player.isOnline())
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

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static void endLoginAttempt(Player player, PlayerInfo playerInfo) {
        if (playerInfo.isLoggedIn()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            player.setLevel(0);
            player.setExp(0);
            player.sendTitle("", "", 0, 0, 0);
        }
    }

    public void startLoginAttempt(Config config, ChannelMessenger channelMessenger, boolean loginStatusAtBeginning) {
        boolean isLoginSessionAvailable = LoginSession.isLoginSessionAvailable(config);
        boolean isBungeeCord = config.get().getBoolean("settings.bungeecord");

        if (loginStatusAtBeginning)
            setCancelled(true);
        else if (isLoginSessionAvailable && playerInfo.isLoginSession(player.getAddress().getAddress()))
            endByLoginSession(channelMessenger, isBungeeCord);
        else
            startWithoutLoginSession(config);
    }

    private void endByLoginSession(ChannelMessenger channelMessenger, boolean isBungeeCord) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Messages.getColoredString("SESSION.ENABLE")));
        if (isBungeeCord)
            channelMessenger.sendLoginStatusChangeMessage(player, true);
        setCancelled(true);
    }

    private void startWithoutLoginSession(Config config) {
        prepareToLoginAttempt(config);
        playerInfo.setLoginStatus(false);
    }

    private void prepareToLoginAttempt(Config config) {
        // Set player's food, health and GameMode according to plugin settings
        if (config.get().getBoolean("settings.join.max_hunger"))
            player.setFoodLevel(20);
        if (config.get().getBoolean("settings.join.max_health"))
            player.setHealth(20);
        if (config.get().getBoolean("settings.join.gamemode.enforce"))
            player.setGameMode(GameMode.valueOf(config.get().getString("settings.join.gamemode.default")));
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
