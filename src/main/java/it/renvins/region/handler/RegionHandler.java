package it.renvins.region.handler;

import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class RegionHandler implements Listener {

    private final IRegionsService regionsService;

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) {
            return;
        }
        if (canInteract(player, event.getClickedBlock().getLocation())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (canInteract(player, event.getBlock().getLocation())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (canInteract(player, event.getBlockPlaced().getLocation())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (canInteract(player, player.getLocation())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (canInteract(player, player.getLocation()) || canInteract(player, event.getRightClicked().getLocation())) {
            return;
        }
        event.setCancelled(true);
    }

    private boolean canInteract(Player player, Location location) {
        Optional<Region> optionalRegion = regionsService.getLocationRegion(location);
        if (optionalRegion.isEmpty()) {
            return true;
        }
        Region region = optionalRegion.get();
        return region.isWhitelisted(player.getName()) || player.hasPermission("region.bypass");
    }

}
