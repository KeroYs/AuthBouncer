package com.github.multidestroy.eventhandlers;

import com.github.multidestroy.configs.Settings;
import com.github.multidestroy.system.PluginSystem;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class LoginSession implements Listener {

    private PluginSystem system;
    private Settings settings;
    public static BaseComponent[] sessionON = TextComponent.fromLegacyText(ChatColor.GREEN + "Session is ON until restart of the server!");
    public static BaseComponent[] sessionHint = TextComponent.fromLegacyText(ChatColor.GOLD + "Login Session can be activated by pressing SHIFT button");

    public LoginSession(PluginSystem system, Settings settings) {
        this.system = system;
        this.settings = settings;
    }

    @EventHandler
    public void onShiftClick(PlayerToggleSneakEvent event) {
        if(settings.session) {
            Player player = event.getPlayer();
            if (event.isSneaking() && system.isPlayerLoggedIn(player.getName())) {
                system.getPlayerInfo(player.getName()).setLoginSession(true);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, sessionON);
            }
        }
    }
}
