package com.github.multidestroy.bungeecord.listeners;

import com.github.multidestroy.bungeecord.Authorization;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DisconnectEvent implements Listener {

    private final Authorization authorization;

    public DisconnectEvent(Authorization authorization) {
        this.authorization = authorization;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        authorization.deAuthorizePlayer(event.getPlayer());
    }
}
