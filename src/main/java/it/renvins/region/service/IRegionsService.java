package it.renvins.region.service;

import it.renvins.region.structure.LazyLocation;
import it.renvins.region.structure.Region;
import it.renvins.region.structure.TemporaryRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IRegionsService extends Loadable {

    CompletableFuture<Boolean> createRegion(Region region);
    CompletableFuture<Boolean> renameRegion(String regionName, String newName);
    CompletableFuture<Boolean> removeRegion(String regionName);

    CompletableFuture<Boolean> redefineLocations(String regionName, LazyLocation loc1, LazyLocation loc2);
    CompletableFuture<Boolean> addWhitelistedPlayer(String username, Region region);
    CompletableFuture<Boolean> removeWhitelistedPlayer(String username, Region region);

    void addToRedefining(Player player, Region region);
    void removeFromRedefining(Player player);

    TemporaryRegion addToSetup(Player player);
    void removeFromSetup(Player player);

    Optional<TemporaryRegion> getTemporaryRegion(Player player);
    Optional<Region> getRedefiningRegion(Player player);
    Optional<Region> getRegion(String name);
    Optional<Region> getLocationRegion(Location location);

    List<Region> getRegions();
}
