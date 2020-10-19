package com.github.multidestroy.bukkit.events.listeners;

import com.github.multidestroy.bukkit.Config;
import com.github.multidestroy.bukkit.i18n.Messages;
import com.github.multidestroy.bukkit.player.PlayerInfo;
import com.github.multidestroy.bukkit.system.PluginSystem;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LoginSession implements Listener {

    private final PluginSystem system;

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

    public static void notifyAboutSessionAccessibility(Player player, PlayerInfo playerInfo, JavaPlugin plugin, Config config) {
        final int[] i = {0};
        final int limit = config.get().getInt("settings.login_session.hint_time");
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!player.isOnline() || playerInfo.isLoginSession())
                    cancel();
                else if (i[0] != limit) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(Messages.getColoredString("SESSION.HINT")));
                    i[0]++;
                } else
                    cancel();
            }
        }.runTaskTimer(plugin, 0, 20);

    }
}
