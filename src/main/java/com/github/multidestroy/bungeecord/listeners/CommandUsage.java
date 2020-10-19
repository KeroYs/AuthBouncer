package com.github.multidestroy.bungeecord.listeners;

import com.github.multidestroy.bungeecord.Authorization;
import com.github.multidestroy.bungeecord.Config;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandUsage implements Listener {

    private final Authorization authorization;
    private final Config config;

    public CommandUsage(Authorization authorization, Config config) {
        this.authorization = authorization;
        this.config = config;
    }

    @EventHandler
    public void onCommandUsage(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            boolean blockade = config.get().getBoolean("authorization_server.bungee_command_lock.enable");

            if (event.isProxyCommand() && blockade) {
                if (!authorization.isPlayerAuthorized(player)) {
                    event.setCancelled(true);
                    String warningMessage = config.get().getString("authorization_server.bungee_command_lock.message");
                    if (warningMessage.length() != 0)
                        player.sendMessage(TextComponent.fromLegacyText(warningMessage));
                }
            }
        }

    }

}
