package com.github.multidestroy.bungeecord;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;

public class ChannelMessenger implements Listener {

    private final String channel;
    private final String joinLobbyChannel;
    private final String authChannel;
    private final Authorization authorization;

    ChannelMessenger(String channel, String joinLobbyChannel, String authChannel, Authorization authorization) {
        this.channel = channel;
        this.joinLobbyChannel = joinLobbyChannel;
        this.authChannel = authChannel;
        this.authorization = authorization;
    }

    public void sendJoinLobbyMessage(Server server, ProxiedPlayer player, boolean loginStatus) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        //Do not send message when server is empty
        if (networkPlayers != null && !networkPlayers.isEmpty()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(joinLobbyChannel);
            out.writeUTF(player.getName());
            out.writeBoolean(loginStatus);

            server.sendData(channel, out.toByteArray());
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) throws IOException {
        if (event.getTag().equals(channel)) {
            if ((event.getSender() instanceof Server)) {
                ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
                DataInputStream in = new DataInputStream(stream);
                String subChannel = in.readUTF();

                if (subChannel.equalsIgnoreCase(authChannel)) {
                    if (event.getReceiver() instanceof ProxiedPlayer) {
                        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                        boolean status = in.readBoolean();

                        if (player.isConnected() && status)
                            authorization.authorizePlayer(player);
                        else
                            authorization.deAuthorizePlayer(player);
                    }
                }
            }
        }
    }

}
