package com.github.multidestroy;

import com.github.multidestroy.player.PlayerInfo;
import com.github.multidestroy.system.LoginAttemptEvent;
import com.github.multidestroy.system.LoginAttemptType;
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

import java.time.Instant;

public class ChannelMessage implements PluginMessageListener {

    private static final String localDisconnectionChannel = "auth_channel";
    private String channel = "bouncer:channel";
    private String joinLobbyChannel = "join_lobby_bukkit";
    private String authChannel = "auth_bungee";
    private JavaPlugin plugin;
    private ThreadSystem threadSystem;
    private PluginSystem system;
    private Config config;

    ChannelMessage(PluginSystem system, ThreadSystem threadSystem, JavaPlugin plugin, Config config) {
        this.system = system;
        this.threadSystem = threadSystem;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        System.out.println("GASDFSAF");
        System.out.println(channel);
        System.out.println(this.channel);
        System.out.println(channel.equals(this.channel));
        if (channel.equalsIgnoreCase(this.channel)) {
            System.out.println("Msg received; " + Instant.now().toEpochMilli());
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subChannel = in.readUTF();
            String playerName = in.readUTF();
            PlayerInfo playerInfo = system.getPlayerInfo(player.getName());
            boolean loginStatus;
            if (subChannel.equalsIgnoreCase(joinLobbyChannel)) {
               loginStatus = in.readBoolean();
               onJoinLobbyMessage(player, playerInfo, loginStatus);
            }
        }
    }

    public void sendLoginStatusChangeMessage(Player player, boolean loginStatus) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(authChannel);
        out.writeBoolean(loginStatus);

        player.sendPluginMessage(plugin, channel, out.toByteArray());
    }

    private void onJoinLobbyMessage(Player player, PlayerInfo playerInfo, boolean loginStatus) {
        //TODO async?
        playerInfo.setLoginStatus(loginStatus);
        boolean isLoginSessionAvailable = config.get().getBoolean("settings.session");
        if (isLoginSessionAvailable && system.isLoginSession(player.getName(), player.getAddress().getAddress())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Messages.getColoredString("SESSION.ENABLE")));
            playerInfo.setLoginStatus(true);
        } else if(!loginStatus) {
            LoginAttemptType loginAttemptType = playerInfo.isRegistered() ? LoginAttemptType.LOGIN : LoginAttemptType.REGISTER;
            Bukkit.getPluginManager().callEvent(new LoginAttemptEvent(player, loginAttemptType));
        }
    }

    /*
    private void handleConnection(String playerName, boolean status) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            threadSystem.lock(playerName);
            try {
                if (system.isPlayerInSystem(playerName)) {

                    Player player = Bukkit.getPlayer(playerName);
                    PlayerInfo playerInfo = system.getPlayerInfo(playerName);
                    system.getPlayerInfo(playerName).setLoginStatus(status);
                    System.out.println(status);
                    if (status) {
                        //Check if login session is enabled in the settings
                        if (isLoginSessionAvailable && system.isLoginSession(player.getName(), player.getAddress().getAddress())) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Messages.getColoredString("SESSION.ENABLE")));
                            system.getPlayerInfo(player.getName()).setLoginStatus(true);
                        }
                    } else {
                        System.out.println("przed eventem");
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                L
                            }
                        }.runTask(plugin);

                    }
                }
            } finally {
                threadSystem.unlock(playerName);
            }
        });
    }

     */

    boolean checkIfSpigot() {
        return plugin.getServer().getVersion().contains( "Spigot" ) || plugin.getServer().getVersion().contains( "Paper" );
    }

    public boolean checkIfBungee(JavaPlugin plugin) {
        return plugin.getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord");
    }

}
