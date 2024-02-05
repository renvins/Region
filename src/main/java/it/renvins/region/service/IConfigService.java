package it.renvins.region.service;

import org.bukkit.configuration.file.FileConfiguration;

public interface IConfigService extends Loadable {

    FileConfiguration getConfig();
    FileConfiguration getLang();
}
