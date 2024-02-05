package it.renvins.region;

import org.bukkit.plugin.java.JavaPlugin;

public class RegionPlugin extends JavaPlugin {

    private final RegionLoader loader = new RegionLoader(this);

    @Override
    public void onEnable() {
        loader.load();
    }

    @Override
    public void onDisable() {
        loader.unload();
    }
}
