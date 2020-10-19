package com.github.multidestroy.bukkit;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;

public class Config {

    private FileConfiguration configuration;
    private final String name;

    public Config(String name) {
        this.name = name;
    }

    public Configuration get() {
        return configuration;
    }

    public void setup(JavaPlugin plugin) {
        System.out.println(plugin.getDataFolder());
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try (InputStream in = plugin.getResource("bukkit/" + name)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        configuration = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Method only for config.yml file
     *
     * @return Number placed between lower and upper compartment(low & top) in same_ip setting
     */

    public short getRandomNumberSameIp() {
        int top = configuration.getInt("settings.login_attempt.ip_blockade.attempts.same_ip.top");
        int low = configuration.getInt("settings.login_attempt.ip_blockade.attempts.same_ip.low");
        return (short) (new Random().nextInt(top - low + 1) + low);
    }

    /**
     * Method only for config.yml file
     *
     * @return Number placed between lower and upper compartment(low & top) in different_ip setting
     */

    public short getRandomNumberDifferentIp() {
        int top = configuration.getInt("settings.login_attempt.ip_blockade.attempts.different_ip.top");
        int low = configuration.getInt("settings.login_attempt.ip_blockade.attempts.different_ip.low");
        return (short) (new Random().nextInt(top - low + 1) + low);
    }

}
