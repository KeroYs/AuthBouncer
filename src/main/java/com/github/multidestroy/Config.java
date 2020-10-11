package com.github.multidestroy;

import com.github.multidestroy.Utils;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Config {

    private FileConfiguration configuration;

    public Configuration get() {
        return configuration;
    }

    public void setup(JavaPlugin plugin) {
        plugin.saveResource("config.yml", false);
        File file = new File(plugin.getDataFolder(), "config.yml");
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * @return Number placed between lower and upper compartment(low & top) in same_ip setting
     */

    public short getRandomNumberSameIp() {
        int top = configuration.getInt("settings.login_attempt.ip_blockade.tries.same_ip.top");
        int low = configuration.getInt("settings.login_attempt.ip_blockade.tries.same_ip.low");
        return (short) (new Random().nextInt(top - low + 1) + low);
    }

    /**
     * @return Number placed between lower and upper compartment(low & top) in different_ip setting
     */

    public short getRandomNumberDifferentIp() {
        int top = configuration.getInt("settings.login_attempt.ip_blockade.tries.different_ip.top");
        int low = configuration.getInt("settings.login_attempt.ip_blockade.tries.different_ip.low");
        return (short) (new Random().nextInt(top - low + 1) + low);
    }

}
