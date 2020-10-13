package com.github.multidestroy;

import com.github.multidestroy.events.LoginAttemptEvent;
import com.github.multidestroy.events.LoginAttemptType;
import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ChannelMessenger implements PluginMessageListener {

    private final String channel = "authbouncer:channel";
    private final JavaPlugin plugin;
    private final PluginSystem system;
    private final Config config;

    ChannelMessenger(PluginSystem system, ThreadSystem threadSystem, JavaPlugin plugin, Config config) {
        this.system = system;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equalsIgnoreCase(this.channel)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subChannel = in.readUTF();
            String playerName = in.readUTF();
            PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
            boolean loginStatus;
            String joinLobbyChannel = "join_lobby_bukkit";
            if (subChannel.equalsIgnoreCase(joinLobbyChannel)) {
                loginStatus = in.readBoolean();
                onJoinLobbyMessage(player, playerInfo, loginStatus);
            }
        }
    }

    public void sendLoginStatusChangeMessage(Player player, boolean loginStatus) {
        if (config.get().getBoolean("settings.bungeecord")) {
            if (player.isOnline()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                String authChannel = "auth_bungee";
                out.writeUTF(authChannel);
                out.writeBoolean(loginStatus);

                player.sendPluginMessage(plugin, channel, out.toByteArray());
            }
        }
    }

    private void onJoinLobbyMessage(Player player, PlayerInfo playerInfo, boolean loginStatus) {
        //TODO async?
        playerInfo.setLoginStatus(loginStatus);
        boolean isLoginSessionAvailable = config.get().getBoolean("settings.login_session.allow");
        if (isLoginSessionAvailable && system.isLoginSession(player.getName(), player.getAddress().getAddress())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Messages.getColoredString("SESSION.ENABLE")));
        } else if (!loginStatus) {
            LoginAttemptType loginAttemptType = playerInfo.isRegistered() ? LoginAttemptType.LOGIN : LoginAttemptType.REGISTER;
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, loginAttemptType));
        }
    }

    boolean checkIfSpigot() {
        return plugin.getServer().getVersion().contains("Spigot") || plugin.getServer().getVersion().contains("Paper");
    }

    public boolean checkIfBungee(JavaPlugin plugin) {
        return plugin.getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord");
    }

}
