package it.renvins.region.service.impl;

import it.renvins.region.RegionLoader;
import it.renvins.region.RegionPlugin;
import it.renvins.region.service.IDatabaseService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.LazyLocation;
import it.renvins.region.structure.Region;
import it.renvins.region.structure.TemporaryRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RegionsService implements IRegionsService {

    private final RegionPlugin plugin;
    private final IDatabaseService databaseService;

    private final Map<UUID, TemporaryRegion> temporaryRegions = new HashMap<>();
    private final Map<UUID, Region> redefiningRegions = new HashMap<>();

    private final List<Region> regions = new ArrayList<>();

    public RegionsService(RegionPlugin plugin, IDatabaseService databaseService) {
        this.plugin = plugin;
        this.databaseService = databaseService;
    }

    @Override
    public void load() {
        RegionLoader.getLogger().info("Loading regions' service...");
        loadRegions();
    }

    @Override
    public CompletableFuture<Boolean> createRegion(Region region) {
        if (getRegion(region.getName()).isPresent()) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(() -> {
            int rows = databaseService.createRegion(region);
            if (rows == -1 || rows == 0) {
                return false;
            }
            regions.add(region);
            return true;
        }, this::runAsync);
    }

    @Override
    public CompletableFuture<Boolean> removeRegion(String regionName) {
        Optional<Region> optionalRegion = getRegion(regionName);
        return optionalRegion.map(region -> CompletableFuture.supplyAsync(() -> {
            int rows = databaseService.removeRegion(regionName);
            if (rows == -1 || rows == 0) {
                return false;
            }
            regions.remove(region);
            return true;
        }, this::runAsync)).orElseGet(() -> CompletableFuture.completedFuture(false));
    }

    @Override
    public CompletableFuture<Boolean> renameRegion(String regionName, String newName) {
        Optional<Region> optionalRegion = getRegion(regionName);
        return optionalRegion.map(region -> CompletableFuture.supplyAsync(() -> {
            int rows = databaseService.renameRegion(regionName, newName);
            if (rows == -1 || rows == 0) {
                return false;
            }
            region.setName(newName);
            return true;
        }, this::runAsync)).orElseGet(() -> CompletableFuture.completedFuture(false));
    }

    @Override
    public CompletableFuture<Boolean> redefineLocations(String regionName, LazyLocation loc1, LazyLocation loc2) {
        Optional<Region> optionalRegion = getRegion(regionName);
        return optionalRegion.map(region -> CompletableFuture.supplyAsync(() -> {
            int rows = databaseService.redefineLocations(regionName, loc1, loc2);
            if (rows == -1 || rows == 0) {
                return false;
            }
            Region newRegion = region.toNewRegion(loc1, loc2);

            regions.remove(region);
            regions.add(newRegion);

            return true;
        }, this::runAsync)).orElseGet(() -> CompletableFuture.completedFuture(false));
    }

    @Override
    public CompletableFuture<Boolean> addWhitelistedPlayer(String username, Region region) {
        if (region.isWhitelisted(username)) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(() -> {
            int rows = databaseService.addWhitelistedPlayer(username, region.getName());
            if (rows == -1 || rows == 0) {
                return false;
            }
            region.addPlayer(username);
            return true;
        }, this::runAsync);
    }

    @Override
    public CompletableFuture<Boolean> removeWhitelistedPlayer(String username, Region region) {
        if (!region.isWhitelisted(username)) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(() -> {
            int rows = databaseService.removeWhitelistedPlayer(username, region.getName());
            if (rows == -1 || rows == 0) {
                return false;
            }
            region.removePlayer(username);
            return true;
        }, this::runAsync);
    }

    @Override
    public void addToRedefining(Player player, Region region) {
        if (getRedefiningRegion(player).isPresent()) {
            return;
        }
        redefiningRegions.put(player.getUniqueId(), region);
    }

    @Override
    public void removeFromRedefining(Player player) {
        if (getRedefiningRegion(player).isEmpty()) {
            return;
        }
        redefiningRegions.remove(player.getUniqueId());
    }

    @Override
    public TemporaryRegion addToSetup(Player player) {
        Optional<TemporaryRegion> optionalTemporaryRegion = getTemporaryRegion(player);
        if (optionalTemporaryRegion.isPresent()) {
            return optionalTemporaryRegion.get();
        }
        TemporaryRegion temporaryRegion = new TemporaryRegion();
        temporaryRegions.put(player.getUniqueId(), temporaryRegion);

        return temporaryRegion;
    }

    @Override
    public void removeFromSetup(Player player) {
        if (getTemporaryRegion(player).isEmpty()) {
            return;
        }
        temporaryRegions.remove(player.getUniqueId());
    }

    @Override
    public Optional<TemporaryRegion> getTemporaryRegion(Player player) {
        if (!temporaryRegions.containsKey(player.getUniqueId())) {
            return Optional.empty();
        }
        return Optional.of(temporaryRegions.get(player.getUniqueId()));
    }

    @Override
    public Optional<Region> getRedefiningRegion(Player player) {
        if (!redefiningRegions.containsKey(player.getUniqueId())) {
            return Optional.empty();
        }
        return Optional.of(redefiningRegions.get(player.getUniqueId()));
    }

    @Override
    public Optional<Region> getRegion(String name) {
        return regions.stream().filter(region -> region.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public Optional<Region> getLocationRegion(Location location) {
        return regions.stream().filter(region -> region.isIn(location)).findFirst();
    }

    @Override
    public List<Region> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    private void loadRegions() {
        databaseService.createRegionsTable().thenCompose(regionsTable -> {
            if (!regionsTable) {
                return CompletableFuture.completedFuture(false);
            }
            return databaseService.createPlayersTable();
        }).thenAccept(playersTable -> {
            if (!playersTable) {
                return;
            }
            List<Region> regions = databaseService.getRegions();
            if (regions == null || regions.isEmpty()) {
                return;
            }
            for (Region region : regions) {
                List<String> whitelist = databaseService.getWhitelist(region.getName());
                if (whitelist == null || whitelist.isEmpty()) {
                    continue;
                }
                region.addPlayers(whitelist);
            }
            this.regions.addAll(regions);
        });
    }

    private void runAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
