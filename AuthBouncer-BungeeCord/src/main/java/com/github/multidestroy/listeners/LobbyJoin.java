package com.github.multidestroy.listeners;

import com.github.multidestroy.Authorization;
import com.github.multidestroy.ChannelMessenger;
import com.github.multidestroy.Config;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LobbyJoin implements Listener {

    private final Authorization authorization;
    private final ChannelMessenger channelMessenger;
    private final Config config;

    public LobbyJoin(Authorization authorization, ChannelMessenger channelMessenger, Config config) {
        this.authorization = authorization;
        this.channelMessenger = channelMessenger;
        this.config = config;
    }

    @EventHandler
    public void onLobbyJoin(ServerConnectedEvent event) {
        String lobbyName = config.get().getString("authorization_server.name");
        if (event.getServer().getInfo().getName().equals(lobbyName))
            channelMessenger.sendJoinLobbyMessage(event.getServer(), event.getPlayer(), authorization.isPlayerAuthorized(event.getPlayer()));
    }

}
