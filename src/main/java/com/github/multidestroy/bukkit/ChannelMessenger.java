package com.github.multidestroy.bukkit;


import com.github.multidestroy.bukkit.events.LoginAttemptEvent;
import com.github.multidestroy.bukkit.events.LoginAttemptType;
import com.github.multidestroy.bukkit.events.listeners.OnJoin;
import com.github.multidestroy.bukkit.i18n.Messages;
import com.github.multidestroy.bukkit.player.PlayerInfo;
import com.github.multidestroy.bukkit.system.PluginSystem;
import com.github.multidestroy.bukkit.system.ThreadSystem;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

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
            PlayerInfo playerInfo = system.getPlayerInfo(playerName);
            boolean loginStatus;
            String joinLobbyChannel = "join_lobby_bukkit";
            if (subChannel.equalsIgnoreCase(joinLobbyChannel)) {
                loginStatus = in.readBoolean();
                onJoinLobbyMessage(player, playerInfo, loginStatus);
            }
        }
    }

    public void sendLoginStatusChangeMessage(Player player, boolean loginStatus) {
        new BukkitRunnable() {

            @Override
            public void run() {
                sendPluginMessageToBungeeCord(player, loginStatus);
            }

        }.runTask(plugin);
    }

    private void onJoinLobbyMessage(Player player, PlayerInfo playerInfo, boolean loginStatus) {
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, playerInfo, config, this, loginStatus));
    }

    boolean checkIfSpigot() {
        return plugin.getServer().getVersion().contains("Spigot") || plugin.getServer().getVersion().contains("Paper");
    }

    public boolean checkIfBungee(JavaPlugin plugin) {
        return plugin.getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord");
    }

    private void sendPluginMessageToBungeeCord(Player player, boolean loginStatus) {
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
}
