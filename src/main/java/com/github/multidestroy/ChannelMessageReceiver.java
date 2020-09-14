package com.github.multidestroy;

import com.github.multidestroy.system.PluginSystem;
import com.github.multidestroy.system.ThreadSystem;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.time.Instant;

public class ChannelMessageReceiver implements PluginMessageListener {

    public static final String globalChannel = "bouncer:channel";
    private static final String localDisconnectionChannel = "disc_channel";
    private JavaPlugin plugin;
    private ThreadSystem threadSystem;
    private PluginSystem pluginSystem;

    public ChannelMessageReceiver(PluginSystem pluginSystem, ThreadSystem threadSystem, JavaPlugin plugin) {
        this.pluginSystem = pluginSystem;
        this.threadSystem = threadSystem;
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equalsIgnoreCase(globalChannel))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput( bytes );
        String subChannel = in.readUTF();
        if(subChannel.equalsIgnoreCase(localDisconnectionChannel))
            onDisconnection(in.readUTF());
    }

    private void onDisconnection(String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                threadSystem.lock(playerName);
                try {
                    if (pluginSystem.isPlayerInSystem(playerName))
                        pluginSystem.getPlayerInfo(playerName).setLoginStatus(false);
                } finally {
                    threadSystem.unlock(playerName);
                }
            }
        });
    }

    public void checkIfBungee(JavaPlugin plugin) {
        //check if the server is Spigot/Paper (because of the spigot.yml file)
        if (!plugin.getServer().getVersion().contains( "Spigot" ) && !plugin.getServer().getVersion().contains( "Paper" )) {
            plugin.getLogger().severe( "You probably run CraftBukkit... Please update atleast to spigot for this to work..." );
            plugin.getLogger().severe( "Plugin disabled!" );
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        if (plugin.getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord")) {
            plugin.getLogger().severe( "This server is not BungeeCord." );
            plugin.getLogger().severe( "If the server is already hooked to BungeeCord, please enable it into your spigot.yml aswell." );
            plugin.getLogger().severe( "Plugin disabled!" );
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

}
