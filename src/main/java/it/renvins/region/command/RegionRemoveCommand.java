package it.renvins.region.command;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class RegionRemoveCommand implements Subcommand {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @Override
    public String getUsage() {
        return "/region remove <region> <player>";
    }

    @Override
    public String getPermission() {
        return "region.remove";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 3) {
            String name = args[1];
            String username = args[2];

            Optional<Region> optionalRegion = regionsService.getRegion(name);
            if (optionalRegion.isEmpty()) {
                Messaging.sendMessage(player, configService.getLang().getString("regionNotFound"));
                return;
            }
            regionsService.removeWhitelistedPlayer(username, optionalRegion.get()).thenAccept(result -> {
                if (!result) {
                    Messaging.sendMessage(player, configService.getLang().getString("cantRemoveFromWhitelist"));
                    return;
                }
                Messaging.sendMessage(player, configService.getLang().getString("removedFromWhitelist"));
            });
            return;
        }
        Messaging.sendMessage(player, configService.getLang().getString("cmdUsage").replaceAll("%usage%", getUsage()));
    }
}
