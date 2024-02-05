package it.renvins.region.service;

import it.renvins.region.structure.LazyLocation;
import it.renvins.region.structure.Region;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IDatabaseService extends Loadable {

    int createRegion(Region region);
    int removeRegion(String regionName);
    int renameRegion(String regionName, String newName);

    int redefineLocations(String name, LazyLocation loc1, LazyLocation loc2);

    int addWhitelistedPlayer(String playerName, String regionName);
    int removeWhitelistedPlayer(String playerName, String regionName);

    Optional<Region> getRegion(String name);
    List<Region> getRegions();

    List<String> getWhitelist(String name);

    CompletableFuture<Boolean> createRegionsTable();
    CompletableFuture<Boolean> createPlayersTable();
}
