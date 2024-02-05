package it.renvins.region.structure;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Region implements IRegion {

    @Getter @Setter private String name;

    private final int xMin;
    private final int xMax;

    private final int yMin;
    private final int yMax;

    private final int zMin;
    private final int zMax;

    @Getter @Setter private LazyLocation loc1;
    @Getter @Setter private LazyLocation loc2;

    private final List<String> whitelistedPlayers = new ArrayList<>();

    public Region(String name, LazyLocation loc1, LazyLocation loc2) {
        this.name = name;

        this.loc1 = loc1;
        this.loc2 = loc2;

        this.xMin = Math.min(loc1.getX(), loc2.getX());
        this.xMax = Math.max(loc1.getX(), loc2.getX());

        this.yMin = Math.min(loc1.getY(), loc2.getY());
        this.yMax = Math.max(loc1.getY(), loc2.getY());

        this.zMin = Math.min(loc1.getZ(), loc2.getZ());
        this.zMax = Math.max(loc1.getZ(), loc2.getZ());
    }

    public void addPlayer(String username) {
        whitelistedPlayers.add(username);
    }

    public void removePlayer(String username) {
        whitelistedPlayers.remove(username);
    }

    public boolean isWhitelisted(String username) {
        return whitelistedPlayers.contains(username);
    }

    public List<String> getWhitelistedPlayers() {
        return Collections.unmodifiableList(whitelistedPlayers);
    }

    public void addPlayers(List<String> players) {
        whitelistedPlayers.addAll(players);
    }

    public boolean isIn(Location loc) {
        return loc.getWorld().getName().equals(this.loc1.getWorldName()) && loc.getX() >= this.xMin && loc.getX() <= this.xMax && loc.getY() >= this.yMin && loc.getY() <= this.yMax && loc
                .getZ() >= this.zMin && loc.getZ() <= this.zMax;
    }

    public Region toNewRegion(LazyLocation loc1, LazyLocation loc2) {
        Region newRegion = new Region(name, loc1, loc2);
        newRegion.addPlayers(whitelistedPlayers);

        return newRegion;
    }
}
