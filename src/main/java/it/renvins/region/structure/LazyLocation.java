package it.renvins.region.structure;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Getter
public class LazyLocation {

    private final int x;
    private final int y;
    private final int z;

    private final String worldName;

    public LazyLocation(int x, int y, int z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.worldName = worldName;
    }

    public LazyLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();

        this.worldName = location.getWorld().getName();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}
