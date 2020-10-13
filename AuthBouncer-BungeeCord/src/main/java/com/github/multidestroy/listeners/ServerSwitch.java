package com.github.multidestroy.listeners;

import com.github.multidestroy.Authorization;
import com.github.multidestroy.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitch implements Listener {

    private final Authorization authorization;
    private final Config config;

    public ServerSwitch(Authorization authorization, Config config) {
        this.authorization = authorization;
        this.config = config;
    }


    @EventHandler
    public void onServerSwitch(ServerConnectEvent event) {
        String lobbyName = config.get().getString("authorization_server.name");
        boolean forceLogin = config.get().getBoolean("authorization_server.force_login");

        if (!event.getTarget().getName().equals(lobbyName))
            if (!authorization.isPlayerAuthorized(event.getPlayer())) {
                if (forceLogin)
                    event.setTarget(ProxyServer.getInstance().getServerInfo(lobbyName));
                else if (event.getPlayer().getServer() != null)
                    event.setCancelled(true);
            }
    }
}
