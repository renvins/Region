package it.renvins.region;

import it.renvins.region.command.RegionsCommand;
import it.renvins.region.handler.RegionHandler;
import it.renvins.region.handler.WandHandler;
import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IDatabaseService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.service.Loadable;
import it.renvins.region.service.impl.ConfigService;
import it.renvins.region.service.impl.DatabaseService;
import it.renvins.region.service.impl.RegionsService;
import lombok.Getter;
import org.bukkit.conversations.ConversationFactory;

import java.util.logging.Logger;

public class RegionLoader implements Loadable {

    private final RegionPlugin plugin;
    @Getter private static Logger logger;
    @Getter private static ConversationFactory conversationFactory;

    private final IConfigService configService;
    private final IDatabaseService databaseService;
    private final IRegionsService regionsService;

    public RegionLoader(RegionPlugin plugin) {
        this.plugin = plugin;

        logger = plugin.getLogger();
        conversationFactory = new ConversationFactory(plugin);

        this.configService = new ConfigService(plugin);
        this.databaseService = new DatabaseService(plugin, configService);
        this.regionsService = new RegionsService(plugin, databaseService);
    }

    @Override
    public void load() {
        configService.load();
        databaseService.load();
        regionsService.load();

        logger.info("Registering commands...");
        plugin.getCommand("region").setExecutor(new RegionsCommand(configService, regionsService));

        logger.info("Registering handlers...");
        plugin.getServer().getPluginManager().registerEvents(new WandHandler(configService, regionsService), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RegionHandler(regionsService), plugin);
    }

    @Override
    public void unload() {
        databaseService.unload();
    }
}
