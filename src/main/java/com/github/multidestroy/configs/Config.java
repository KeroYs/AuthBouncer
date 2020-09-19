package com.github.multidestroy.configs;

import com.github.multidestroy.Utils;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private FileConfiguration configuration;
    private File file;

    public Map<String, String> getDataBaseInfo() {
        Map<String, String> dataBase = new HashMap<>();
        dataBase.put("host", configuration.getString("database.host"));
        dataBase.put("port", configuration.getString("database.port"));
        dataBase.put("name", configuration.getString("database.name"));
        dataBase.put("username", configuration.getString("database.username"));
        dataBase.put("password", configuration.getString("database.password"));

        return dataBase;
    }

    public String getMessage(String commandOrSession) {
        return Utils.mergeListWithNewLines(configuration.getStringList("messages." + commandOrSession));
    }

    public boolean getBooleanSetting(String path) {
        return configuration.getBoolean(path);
    }

    public int getIntegerSetting(String path) {
        return configuration.getInt(path);
    }

    public Configuration get() {
        return configuration;
    }

    public void setup(File dataFolder, JavaPlugin plugin) {
        plugin.saveResource("config.yml", false);
        file = new File(dataFolder, "config.yml");
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
