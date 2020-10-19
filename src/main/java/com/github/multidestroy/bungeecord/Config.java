package com.github.multidestroy.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Config {

    private Configuration config;
    private File configFile;

    public Configuration get() {
        return config;
    }

    void setup(File dataFolder, Plugin plugin) {
        if (!dataFolder.exists())
            dataFolder.mkdir();

        if (configFile == null)
            configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) {
            try (InputStream in = plugin.getResourceAsStream("bungeecord/config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
