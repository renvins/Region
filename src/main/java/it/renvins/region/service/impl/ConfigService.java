package it.renvins.region.service.impl;

import it.renvins.region.RegionLoader;
import it.renvins.region.RegionPlugin;
import it.renvins.region.service.IConfigService;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigService implements IConfigService {

    private final RegionPlugin plugin;

    private final File configFile;
    @Getter private FileConfiguration config;

    private final File langFile;
    @Getter private FileConfiguration lang;

    public ConfigService(RegionPlugin plugin) {
        this.plugin = plugin;

        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.langFile = new File(plugin.getDataFolder(), "lang.yml");
    }

    @Override
    public void load() {
        RegionLoader.getLogger().info("Loading configs...");
        loadConfigs();
    }

    private void createConfig(File configFile) {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(configFile.getName(), false);
        }
    }

    private void loadConfigs() {
        createConfig(configFile);
        createConfig(langFile);

        config = YamlConfiguration.loadConfiguration(configFile);
        lang = YamlConfiguration.loadConfiguration(langFile);
    }
}
