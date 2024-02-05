package it.renvins.region.command;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class RegionAddCommand implements Subcommand {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @Override
    public String getUsage() {
        return "/region add <region> <player>";
    }

    @Override
    public String getPermission() {
        return "region.add";
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
            regionsService.addWhitelistedPlayer(username, optionalRegion.get()).thenAccept(result -> {
                if (!result) {
                    Messaging.sendMessage(player, configService.getLang().getString("cantAddWhitelist"));
                    return;
                }
                Messaging.sendMessage(player, configService.getLang().getString("addedWhitelist"));
            });
            return;
        }
        Messaging.sendMessage(player, configService.getLang().getString("cmdUsage").replaceAll("%usage%", getUsage()));
    }
}
