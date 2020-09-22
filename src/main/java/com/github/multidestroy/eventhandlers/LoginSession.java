package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.Messages;
import com.github.multidestroy.configs.Config;
import com.github.multidestroy.system.PluginSystem;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LoginSession implements Listener {

    private PluginSystem system;
    public LoginSession(PluginSystem system) {
        this.system = system;
    }

    @EventHandler
    public void onShiftClick(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking() && system.isPlayerLoggedIn(player.getName())) {
            system.getPlayerInfo(player.getName()).setLoginSession(true);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Messages.getColoredString("SESSION.ENABLE")));
        }
    }

    public static void notifyAboutSessionAccessibility(Player player, JavaPlugin plugin) {
        final int[] i = {0};
        new BukkitRunnable() {

            @Override
            public void run() {
                if(i[0] != 3) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(Messages.getColoredString("SESSION.HINT")));
                    i[0]++;
                } else
                    cancel();
            }
        }.runTaskTimer(plugin, 0, 20);

    }
}
