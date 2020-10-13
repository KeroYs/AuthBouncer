package com.github.multidestroy;

import com.github.multidestroy.listeners.CommandUsage;
import com.github.multidestroy.listeners.DisconnectEvent;
import com.github.multidestroy.listeners.LobbyJoin;
import com.github.multidestroy.listeners.ServerSwitch;
import net.md_5.bungee.api.plugin.Plugin;

final class MainPluginClass extends Plugin {

    private Authorization authorization;

    @Override
    public void onEnable() {
        String channel = "authbouncer:channel";
        String joinLobbyChannel = "join_lobby_bukkit";
        String authChannel = "auth_bungee";

        Config config = new Config();
        config.setup(getDataFolder(), this);
        authorization = new Authorization();
        ChannelMessenger channelMessenger = new ChannelMessenger(channel, joinLobbyChannel, authChannel, authorization);
        getProxy().registerChannel(channel);
        getProxy().getPluginManager().registerListener(this, channelMessenger);
        getProxy().getPluginManager().registerListener(this, new CommandUsage(authorization, config));
        getProxy().getPluginManager().registerListener(this, new DisconnectEvent(authorization));
        getProxy().getPluginManager().registerListener(this, new LobbyJoin(authorization, channelMessenger, config));
        getProxy().getPluginManager().registerListener(this, new ServerSwitch(authorization, config));
        getProxy().getPluginManager().registerListener(this, channelMessenger);
    }

    @Override
    public void onDisable() {
        getProxy().getServers().values().forEach(serverInfo ->
                serverInfo.getPlayers().forEach(player ->
                        authorization.deAuthorizePlayer(player)));
    }
}
