package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.configs.Config;
import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
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
import org.bukkit.scheduler.BukkitScheduler;

import java.net.InetAddress;

public class LoginAttempt implements Listener {

    private Settings settings;
    private PluginSystem system;
    private JavaPlugin plugin;
    private ThreadSystem passwordThreadSystem;
    private Config config;

    public LoginAttempt(Settings settings, PluginSystem system, JavaPlugin plugin, ThreadSystem passwordThreadSystem, Config config) {
        this.settings = settings;
        this.system = system;
        this.plugin = plugin;
        this.passwordThreadSystem = passwordThreadSystem;
        this.config = config;
    }

    /**
     * Starts a timer with the time limit (set in the config) for a player to log in.
     * Timer is represented by a XP bar and the slow and blindness effect will be set on the player if it was enabled in config!
     */

    @EventHandler
    public void loginAttempt(LoginAttemptEvent event) {
        Player player = event.getPlayer();
        PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
        InetAddress ipAddress = player.getAddress().getAddress();
        LoginAttemptType loginAttemptType = event.getType();
        final int[] iteration = { 1 };
        short time = settings.login_attempt_time;
        float subtrahend = 1f / (float) time;

        setPotionEffects(player);
        {
            int period = config.get().getInt("settings.login_attempt.title_text.display_time.period");
            setTitle(event, player, playerInfo).runTaskTimer(plugin, 0, period);
        }
        player.setLevel(time);
        player.setExp(1);
        {

            //Sound configuration
            Sound sound = Sound.valueOf(config.get().getString("settings.login_attempt.sound.name"));
            boolean soundIncreasingVolume = config.get().getBoolean("settings.login_attempt.sound.increasing_volume");
            float soundMaxVolume = (float) config.get().getInt("settings.login_attempt.sound.max_volume") / 100;
            int soundPitch = config.get().getInt("settings.login_attempt.sound.pitch");

            new BukkitRunnable() {

                public void run() {

                    if (!player.isOnline()) {
                        removePotionEffects(player);
                        cancel();
                    } else if (loginAttemptType == LoginAttemptType.REGISTER && playerInfo.isRegistered() ||
                            loginAttemptType == LoginAttemptType.LOGIN && playerInfo.isLoggedIn()) {
                        player.resetTitle();
                        removePotionEffects(player);
                        cancel();
                    } else if (iteration[0] == time) {
                        endAfterCommandExecution(player, playerInfo, event).runTaskAsynchronously(plugin);
                        cancel();
                    }

                    if (settings.ip_blockade)
                        if (playerInfo.canNotifyAboutSoonBlockade(settings, ipAddress))
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    TextComponent.fromLegacyText(ChatColor.GOLD + "Your IP address on that account might be blocked in a few next attempts"));


                    if(soundMaxVolume != 0)
                        playSound(player, sound, soundMaxVolume, soundIncreasingVolume, soundPitch, iteration[0], time);

                    //Next iteration
                    iteration[0]++;
                    player.setLevel(player.getLevel() - 1);
                    player.setExp(player.getExp() - subtrahend > 0 ? player.getExp() - subtrahend : 0);
                }
            }.runTaskTimer(plugin, 0, 20); //period: 20 ticks mean 1 second
        }
    }

    @EventHandler
    public void stopDamageDuringLoginAttempt(EntityDamageEvent event) {
        if(event.getEntityType() == EntityType.PLAYER)
            if(!system.isPlayerLoggedIn(event.getEntity().getName()))
                event.setCancelled(true);
    }

    private BukkitRunnable endAfterCommandExecution(Player player, PlayerInfo playerInfo, LoginAttemptEvent event) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                passwordThreadSystem.lock(player.getName());
                try {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.setLevel(0);
                        player.setExp(0);
                        removePotionEffects(player);
                        if (playerInfo.isLoggedIn()) {
                            player.resetTitle();
                            if (settings.session)
                                event.notifyAboutLoginSessionAccessibility();
                        } else
                            event.disallow();
                    });
                } finally {
                    passwordThreadSystem.unlock(player.getName());
                }
            }
        };
    }

    private BukkitRunnable setTitle(LoginAttemptEvent event, Player player, PlayerInfo playerInfo) {
        String title;
        String subtitle;
        int fadeInTime = config.get().getInt("settings.login_attempt.title_text.display_time.fade_in");
        int stayTime = config.get().getInt("settings.login_attempt.title_text.display_time.stay");
        int fadeOutTime = config.get().getInt("settings.login_attempt.title_text.display_time.fade_out");

        if (event.getType() == LoginAttemptType.LOGIN) {
            title = ChatColor.translateAlternateColorCodes('§', config.get().getString("settings.login_attempt.title_text.login.title"));
            subtitle = ChatColor.translateAlternateColorCodes('§', config.get().getString("settings.login_attempt.title_text.login.subtitle"));
        } else {
            title = ChatColor.translateAlternateColorCodes('§', config.get().getString("settings.login_attempt.title_text.register.title"));
            subtitle = ChatColor.translateAlternateColorCodes('§', config.get().getString("settings.login_attempt.title_text.register.subtitle"));
        }

        return new BukkitRunnable() {

            @Override
            public void run() {
                if(title.length() == 0 && subtitle.length() == 0)
                    cancel();

                if(!event.isCancelled())
                    player.sendTitle(title, subtitle, fadeInTime, stayTime, fadeOutTime);
                else
                    cancel();
            }

        };
    }

    private void setPotionEffects(Player player) {
        if (settings.blindness)
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, settings.login_attempt_time * 21, 4));
        if (settings.slowness)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, settings.login_attempt_time * 21, 4));
    }

    private void removePotionEffects(Player player) {
        if (settings.blindness)
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        if (settings.slowness)
            player.removePotionEffect(PotionEffectType.SLOW);
    }

    private void playSound(Player player, Sound sound, float maxVolume, boolean increasingVolume, int pitch, int iteration, int time) {
        float volume;
        if(increasingVolume) {
            volume = (float) iteration / time;
            if(volume > maxVolume)
                volume = maxVolume;
        } else
            volume = maxVolume;

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

}
