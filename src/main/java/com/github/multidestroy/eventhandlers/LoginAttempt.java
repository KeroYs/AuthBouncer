package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;

public class LoginAttempt implements Listener {

    private Settings settings;
    private PluginSystem system;
    private JavaPlugin plugin;
    private ThreadSystem passwordThreadSystem;

    public LoginAttempt(Settings settings, PluginSystem system, JavaPlugin plugin, ThreadSystem passwordThreadSystem) {
        this.settings = settings;
        this.system = system;
        this.plugin = plugin;
        this.passwordThreadSystem = passwordThreadSystem;
    }

    /**
     * Starts a timer with the time limit (set in the config) for a player to log in.
     * Timer is represented by a XP bar and the slow and blindness effect will be set on the player if it was enabled in config!
     */

    @EventHandler
    public void loginAttempt(LoginAttemptEvent event) {
        Player player = event.getPlayer();
        short time = settings.login_attempt_time;
        if (settings.blindness)
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, time * 21, 4));
        if (settings.slowness)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time * 21, 4));

        float subtrahend = 1f / (float) time;
        player.setLevel(time);
        player.setExp(1);
        PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
        InetAddress ipAddress = player.getAddress().getAddress();
        new BukkitRunnable() {

                    public void run() {
                        if(settings.ip_blockade)
                            if(playerInfo.canNotifyAboutSoonBlockade(settings, ipAddress))
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        TextComponent.fromLegacyText(ChatColor.GOLD + "Your IP address on that account might be blocked in a few next attempts"));

                        player.setLevel(player.getLevel() - 1);
                        float minusXP;
                        player.setExp((minusXP =  player.getExp() - subtrahend) > 0 ? minusXP : 0);
                        if(player.getLevel() != 0)
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f / player.getLevel(), 0);
                        else
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);

                        if (player.getExp() == 0 || !player.isOnline() || system.isPlayerLoggedIn(player.getName())) {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, endLoginAttemptWhenNoCommandExecuting(player, playerInfo, event));
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 20); //period: 20 ticks mean 1 second
    }

    @EventHandler
    public void stopDamageDuringLoginAttempt(EntityDamageEvent event) {
        if(event.getEntityType() == EntityType.PLAYER)
            if(!system.isPlayerLoggedIn(event.getEntity().getName()))
                event.setCancelled(true);
    }

    private Runnable endLoginAttemptWhenNoCommandExecuting(Player player, PlayerInfo playerInfo, LoginAttemptEvent event) {
        return () -> {
            passwordThreadSystem.lock(player.getName());
            try {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.setLevel(0);
                    player.setExp(0);
                    if (settings.blindness)
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    if (settings.slowness)
                        player.removePotionEffect(PotionEffectType.SLOW);
                    if (playerInfo.isLoggedIn()) {
                        if (settings.session)
                            event.notifyAboutLoginSessionAccessibility();
                    } else
                        event.disallow();
                });
            } finally {
                passwordThreadSystem.unlock(player.getName());
            }
        };
    }

}
