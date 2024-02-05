package it.renvins.region.handler;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.IRegion;
import it.renvins.region.structure.LazyLocation;
import it.renvins.region.structure.Region;
import it.renvins.region.util.ItemBuilder;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@RequiredArgsConstructor
public class WandHandler implements Listener {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        ItemStack wand = ItemBuilder.createItem(configService.getConfig().getConfigurationSection("setup"), "wand", null);
        if (!item.isSimilar(wand)) {
            return;
        }
        event.setCancelled(true);

        IRegion region;
        Optional<Region> optionalRedefining = regionsService.getRedefiningRegion(player);
        if (optionalRedefining.isEmpty()) {
            region = regionsService.addToSetup(player);
        } else {
            region = optionalRedefining.get();
        }

        Action action = event.getAction();
        Location location = player.getLocation();
        if (action == Action.LEFT_CLICK_AIR) {
            if (optionalRedefining.isPresent()) {
                if (player.isSneaking()) {
                    Region redefiningRegion = (Region) region;
                    regionsService.redefineLocations(redefiningRegion.getName(), redefiningRegion.getLoc1(), redefiningRegion.getLoc2()).thenAccept(result -> {
                        regionsService.removeFromRedefining(player);
                        if (!result) {
                            Messaging.sendMessage(player, configService.getLang().getString("cantRedefineRegion"));
                            return;
                        }
                        Messaging.sendMessage(player, configService.getLang().getString("regionRedefined"));
                    });
                    return;
                }
            }
            region.setLoc1(new LazyLocation(location));
            Messaging.sendMessage(player, configService.getLang().getString("setLoc1"));
        }
        if (action == Action.RIGHT_CLICK_AIR) {
            region.setLoc2(new LazyLocation(location));
            Messaging.sendMessage(player, configService.getLang().getString("setLoc2"));
        }
    }
}
