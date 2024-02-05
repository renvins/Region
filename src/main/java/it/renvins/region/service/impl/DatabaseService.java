package it.renvins.region.service.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.renvins.region.RegionLoader;
import it.renvins.region.RegionPlugin;
import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IDatabaseService;
import it.renvins.region.structure.LazyLocation;
import it.renvins.region.structure.Region;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/* Use all the methods in this class async */
@RequiredArgsConstructor
public class DatabaseService implements IDatabaseService {

    private final RegionPlugin plugin;
    private final IConfigService configService;

    private HikariDataSource dataSource;

    @Override
    public void load() {
        RegionLoader.getLogger().info("Connecting to database...");
        loadDataSource();
    }

    @Override
    public void unload() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public int createRegion(Region region) {
        String sql = "INSERT INTO regions(name, loc1x, loc1y, loc1z, loc2x, loc2y, loc2z, world) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, region.getName());

            setLocationInStatement(statement, region.getLoc1(), 2);
            setLocationInStatement(statement, region.getLoc2(), 5);

            statement.setString(8, region.getLoc1().getWorldName());
            return statement.executeUpdate();
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't insert region " + region.getName() + " in database!", e);
            return -1;
        }
    }

    @Override
    public int removeRegion(String regionName) {
        String sql = "DELETE FROM regions WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, regionName);

            return statement.executeUpdate();
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't delete region " + regionName + " from database!", e);
            return -1;
        }
    }

    @Override
    public int renameRegion(String regionName, String newName) {
        String sql = "UPDATE regions SET name = ? WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, newName);
            statement.setString(2, regionName);

            return statement.executeUpdate();
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't update " + regionName + " from database!", e);
            return -1;
        }
    }

    @Override
    public int redefineLocations(String name, LazyLocation loc1, LazyLocation loc2) {
        String sql = "UPDATE regions SET loc1x = ?, loc1y = ?, loc1z = ?, loc2x = ?, loc2y = ?, loc2z = ?, world = ? WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            setLocationInStatement(statement, loc1, 1);
            setLocationInStatement(statement, loc2, 4);

            statement.setString(7, loc1.getWorldName());
            statement.setString(8, name);
            return statement.executeUpdate();
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't redefine region " + name + " in database!", e);
            return -1;
        }
    }

    @Override
    public int addWhitelistedPlayer(String playerName, String regionName) {
        String sql = "INSERT INTO regions_whitelist(player, id) VALUES(?, (SELECT id FROM regions WHERE name = ?))";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, playerName);
            statement.setString(2, regionName);

            return statement.executeUpdate();
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't insert a player in whitelist of " + regionName + " in database!", e);
            return -1;
        }
    }


    @Override
    public int removeWhitelistedPlayer(String playerName, String regionName) {
        String sql = "DELETE FROM regions_whitelist WHERE player = ? AND id = (SELECT id FROM regions WHERE name = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, playerName);
            statement.setString(2, regionName);

            return statement.executeUpdate();
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't delete whitelisted player from " + regionName + " from database!", e);
            return -1;
        }
    }

    @Override
    public Optional<Region> getRegion(String name) {
        String sql = "SELECT * FROM regions WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);

            ResultSet rs = statement.executeQuery();
            String world = rs.getString("world");

            LazyLocation loc1 = new LazyLocation(rs.getInt("loc1x"), rs.getInt("loc1y"), rs.getInt("loc1z"), world);
            LazyLocation loc2 = new LazyLocation(rs.getInt("loc2x"), rs.getInt("loc2y"), rs.getInt("loc2z"), world);

            Region region = new Region(name, loc1, loc2);
            return Optional.of(region);
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't get region " + name + " from database!", e);
            return Optional.empty();
        }
    }

    @Override
    public List<String> getWhitelist(String regionName) {
        String sql = "SELECT regions_whitelist.player, regions_whitelist.id FROM regions_whitelist"
                + " INNER JOIN regions ON regions_whitelist.id = regions.id WHERE regions.name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, regionName);

            List<String> whitelist = new ArrayList<>();
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                whitelist.add(rs.getString("player"));
            }
            return whitelist;
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't get whitelist for " + regionName + " from database!", e);
            return null;
        }
    }

    @Override
    public List<Region> getRegions() {
        String sql = "SELECT * FROM regions";
        List<Region> regions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            if (rs == null) {
                return regions;
            }
            while (rs.next()) {
                String name = rs.getString("name");
                String world = rs.getString("world");

                LazyLocation loc1 = new LazyLocation(rs.getInt("loc1x"), rs.getInt("loc1y"), rs.getInt("loc1z"), world);
                LazyLocation loc2 = new LazyLocation(rs.getInt("loc2x"), rs.getInt("loc2y"), rs.getInt("loc2z"), world);

                Region region = new Region(name, loc1, loc2);
                regions.add(region);
            }
            return regions;
        } catch (SQLException e) {
            RegionLoader.getLogger().log(Level.SEVERE, "Can't get regions from database!", e);
            return null;
        }
    }

    @Override
    public CompletableFuture<Boolean> createRegionsTable() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "CREATE TABLE IF NOT EXISTS regions(id INT NOT NULL AUTO_INCREMENT, name VARCHAR(36) NOT NULL, loc1x INTEGER NOT NULL, " +
                    "loc1y INTEGER NOT NULL, loc1z INTEGER NOT NULL, loc2x INTEGER NOT NULL, loc2y INTEGER NOT NULL, loc2z INTEGER NOT NULL, " +
                    "world VARCHAR(36) NOT NULL, PRIMARY KEY (id))";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();

                return true;
            } catch (SQLException e) {
                RegionLoader.getLogger().log(Level.SEVERE, "Can't create regions table!", e);
                RegionLoader.getLogger().severe("Disabling plugin...");

                plugin.getPluginLoader().disablePlugin(plugin);
                return false;
            }
        }, this::runAsync);
    }

    @Override
    public CompletableFuture<Boolean> createPlayersTable() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "CREATE TABLE IF NOT EXISTS regions_whitelist(player VARCHAR(36) NOT NULL, id INT NOT NULL, FOREIGN KEY (id) REFERENCES regions(id))";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                RegionLoader.getLogger().log(Level.SEVERE, "Can't create regions_whitelist table!", e);
                RegionLoader.getLogger().severe("Disabling plugin...");

                plugin.getPluginLoader().disablePlugin(plugin);
                return false;
            }
        }, this::runAsync);
    }

    private void setLocationInStatement(PreparedStatement statement, LazyLocation loc, int indexStart) throws SQLException {
        statement.setInt(indexStart, loc.getX());
        statement.setInt(indexStart + 1, loc.getY());
        statement.setInt(indexStart + 2, loc.getZ());
    }

    private void loadDataSource() {
        ConfigurationSection dbSection = configService.getConfig().getConfigurationSection("database");
        if (dbSection == null) {
            RegionLoader.getLogger().severe("Can't find data to create database connection!");
            RegionLoader.getLogger().severe("Disabling plugin...");

            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }
        String host = dbSection.getString("host");
        String database = dbSection.getString("database");

        String url = "jdbc:mysql://" + host + "/" + database;

        String user = dbSection.getString("user");
        String password = dbSection.getString("password");

        dataSource = new HikariDataSource(getHikariConfig(url, user, password));
    }

    private HikariConfig getHikariConfig(String url, String user, String password) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(url);

        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        return hikariConfig;
    }

    private void runAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
