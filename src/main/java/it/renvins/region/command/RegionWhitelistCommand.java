package it.renvins.region.command;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class RegionWhitelistCommand implements Subcommand {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @Override
    public String getUsage() {
        return "/region whitelist <region>";
    }

    @Override
    public String getPermission() {
        return "region.whitelist";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 2) {
            String name = args[1];
            Optional<Region> optionalRegion = regionsService.getRegion(name);

            if (optionalRegion.isEmpty()) {
                Messaging.sendMessage(player, configService.getLang().getString("regionNotFound"));
                return;
            }
            Region region = optionalRegion.get();
            String usernames = String.join(", ", region.getWhitelistedPlayers());

            Messaging.sendMessage(player, configService.getLang().getString("whitelistFormat").replaceAll("%whitelist%", usernames));
            return;
        }
        Messaging.sendMessage(player, configService.getLang().getString("cmdUsage").replaceAll("%usage%", getUsage()));
    }
}
