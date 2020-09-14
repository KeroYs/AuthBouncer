package com.github.multidestroy.configs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class EmailConfig {

    private FileConfiguration configuration;
    private File file;

    public String getSender() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public String getMessage() {
        return null;
    }

    public void setup(File dataFolder, JavaPlugin plugin) {
        plugin.saveResource("email_config.yml", false);
        file = new File(dataFolder, "email_config.yml");
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
