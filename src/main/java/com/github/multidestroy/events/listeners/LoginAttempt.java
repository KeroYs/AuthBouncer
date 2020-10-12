package com.github.multidestroy.listeners;

import com.github.multidestroy.Messages;
import com.github.multidestroy.Config;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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

    private PluginSystem system;
    private JavaPlugin plugin;
    private ThreadSystem passwordThreadSystem;
    private Config config;

    public LoginAttempt(PluginSystem system, JavaPlugin plugin, ThreadSystem passwordThreadSystem, Config config) {
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
        int time = config.get().getInt("settings.login_attempt.time");
        float subtrahend = 1f / (float) time;

        setPotionEffects(player, time);
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

            //Ip allow
            boolean isIpBlockadeON = config.get().getBoolean("settings.login_attempt.ip_blockade.allow");

            new BukkitRunnable() {

                public void run() {

                    if (!player.isOnline()) {
                        event.setCancelled(true);
                        cancel();
                    } else if (loginAttemptType == LoginAttemptType.REGISTER && playerInfo.isRegistered() ||
                            loginAttemptType == LoginAttemptType.LOGIN && playerInfo.isLoggedIn()) {
                        event.setCancelled(true);
                        cancel();
                    } else if (iteration[0] == time) {
                        endAfterCommandExecution(player, playerInfo, event).runTaskAsynchronously(plugin);
                        cancel();
                    } else {

                        if (isIpBlockadeON)
                            if (playerInfo.canNotifyAboutSoonBlockade(config, ipAddress))
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        TextComponent.fromLegacyText(Messages.getColoredString("IP_BLOCKADE.NOTIFICATION")));


                        if (soundMaxVolume != 0)
                            playSound(player, sound, soundMaxVolume, soundIncreasingVolume, soundPitch, iteration[0], time);

                        //Next iteration
                        player.setLevel(time - iteration[0]);
                        player.setExp(1 - (float) iteration[0] / time);
                        iteration[0]++;
                    }
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
                boolean isLoginSessionAvailable = config.get().getBoolean("settings.login_session");
                passwordThreadSystem.lock(player.getName());
                try {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!playerInfo.isLoggedIn())
                            event.disallow();

                        event.setCancelled(true);
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
            title = Messages.getColoredString("LOGIN_ATTEMPT.LOGIN.TITLE");
            subtitle = Messages.getColoredString("LOGIN_ATTEMPT.LOGIN.SUBTITLE");
        } else {
            title = Messages.getColoredString("LOGIN_ATTEMPT.REGISTER.TITLE");
            subtitle = Messages.getColoredString("LOGIN_ATTEMPT.REGISTER.SUBTITLE");
        }

        return new BukkitRunnable() {

            @Override
            public void run() {
                if(title.length() == 0 && subtitle.length() == 0)
                    cancel();

                if(!event.isCancelled() && !playerInfo.isLoggedIn())
                    player.sendTitle(title, subtitle, fadeInTime, stayTime, fadeOutTime);
                else
                    cancel();
            }

        };
    }

    private void setPotionEffects(Player player, int time) {
        boolean blindness = config.get().getBoolean("settings.login_attempt.blindness");
        boolean slowness = config.get().getBoolean("settings.login_attempt.slowness");

        if (blindness)
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (time + 1) * 20, 4), true);
        if (slowness)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (time + 1) * 20, 4), true);
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
