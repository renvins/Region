package it.renvins.region.command;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.structure.TemporaryRegion;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class RegionCreateCommand implements Subcommand {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @Override
    public String getUsage() {
        return "/region create <name>";
    }

    @Override
    public String getPermission() {
        return "region.create";
    }

    @Override
    public void execute(Player player, String[] args) {
        if(args.length == 2) {
            String name = args[1];
            Optional<TemporaryRegion> optionalRegion = regionsService.getTemporaryRegion(player);

            if (optionalRegion.isEmpty()) {
                Messaging.sendMessage(player, configService.getLang().getString("notInSetup"));
                return;
            }
            TemporaryRegion temporaryRegion = optionalRegion.get();
            Region region = temporaryRegion.toRegion(name);

            if (region == null) {
                Messaging.sendMessage(player, configService.getLang().getString("completeSetup"));
                return;
            }
            regionsService.createRegion(region).thenAccept(result -> {
                regionsService.removeFromSetup(player);
                if (!result) {
                    Messaging.sendMessage(player, configService.getLang().getString("cantCreateRegion"));
                    return;
                }
                Messaging.sendMessage(player, configService.getLang().getString("regionCreated"));
            });
            return;
        }
        Messaging.sendMessage(player, configService.getLang().getString("cmdUsage").replaceAll("%usage%", getUsage()));
    }
}
