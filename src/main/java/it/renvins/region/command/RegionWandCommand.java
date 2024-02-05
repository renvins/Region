package it.renvins.region.command;

import it.renvins.region.service.IConfigService;
import it.renvins.region.util.ItemBuilder;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class RegionWandCommand implements Subcommand {

    private final IConfigService configService;

    @Override
    public String getUsage() {
        return "/region wand";
    }

    @Override
    public String getPermission() {
        return "region.create";
    }

    @Override
    public void execute(Player player, String[] args) {
        if(args.length == 1) {
            ItemStack item = ItemBuilder.createItem(configService.getConfig().getConfigurationSection("setup"), "wand", null);
            player.getInventory().addItem(item);

            Messaging.sendMessage(player, configService.getLang().getString("wandGiven"));
            return;
        }
        Messaging.sendMessage(player, configService.getLang().getString("cmdUsage").replaceAll("%usage%", getUsage()));
    }
}
