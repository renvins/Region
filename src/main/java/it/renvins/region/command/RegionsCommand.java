package it.renvins.region.command;

import it.renvins.region.menu.RegionMenu;
import it.renvins.region.menu.RegionsMenu;
import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.Messaging;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegionsCommand implements CommandExecutor {

    private final IConfigService configService;
    private final IRegionsService regionService;

    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public RegionsCommand(IConfigService configService, IRegionsService regionService) {
        this.configService = configService;
        this.regionService = regionService;

        subcommands.put("wand", new RegionWandCommand(configService));
        subcommands.put("create", new RegionCreateCommand(configService, regionService));

        subcommands.put("add", new RegionAddCommand(configService, regionService));
        subcommands.put("remove", new RegionRemoveCommand(configService, regionService));

        subcommands.put("whitelist", new RegionWhitelistCommand(configService, regionService));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length > 0) {
                String firstArg = args[0];
                if (!subcommands.containsKey(firstArg)) {

                    Optional<Region> optionalRegion = regionService.getRegion(firstArg);
                    if (optionalRegion.isEmpty()) {
                        Messaging.sendMessage(player, configService.getLang().getString("commandNotFound"));
                        return true;
                    }
                    new RegionMenu(configService, regionService, optionalRegion.get(), player).init();
                    return true;
                }
                Subcommand command = subcommands.get(firstArg);
                if (!player.hasPermission(command.getPermission())) {
                    Messaging.sendMessage(player, configService.getLang().getString("noPermission"));
                    return true;
                }
                command.execute(player, args);

                return true;
            }
            if (!player.hasPermission("region.menu")) {
                Messaging.sendMessage(player, configService.getLang().getString("noPermission"));
                return true;
            }
            new RegionsMenu(configService, regionService).init(player);

            return true;
        }
        Messaging.sendMessage(sender, configService.getLang().getString("onlyPlayers"));
        return true;
    }
}
