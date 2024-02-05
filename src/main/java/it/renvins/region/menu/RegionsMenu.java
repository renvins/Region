package it.renvins.region.menu;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.ItemBuilder;
import it.renvins.region.util.Messaging;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegionsMenu {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    private final ConfigurationSection guiSection;
    private final ChestGui gui;

    public RegionsMenu(IConfigService configService, IRegionsService regionsService) {
        this.configService = configService;
        this.regionsService = regionsService;

        this.guiSection = configService.getConfig().getConfigurationSection("regionsMenu");
        gui = new ChestGui(6,
                Messaging.color(guiSection.getString("title")));

    }

    public void init(Player player) {
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        ConfigurationSection itemSection = guiSection.getConfigurationSection("items");
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 5);

        List<Region> regions = regionsService.getRegions();
        List<GuiItem> guiItems = new ArrayList<>();

        regions.forEach(region -> {
            GuiItem guiItem = new GuiItem(ItemBuilder.createItem(itemSection, "region", Map.of("%region%", region.getName())),
                    event -> new RegionMenu(configService, regionsService, region, player).init());
            guiItems.add(guiItem);
        });
        paginatedPane.populateWithGuiItems(guiItems);

        StaticPane pagesPane = new StaticPane(0, 5, 9, 1);
        pagesPane.addItem(new GuiItem(ItemBuilder.createItem(itemSection, "pageBefore", null), event -> {
            if (paginatedPane.getPage() > 0) {
                paginatedPane.setPage(paginatedPane.getPage() - 1);

                gui.update();
            }
        }), 0, 0);
        pagesPane.addItem(new GuiItem(ItemBuilder.createItem(itemSection, "pageAfter", null), event -> {
            if (paginatedPane.getPage() < paginatedPane.getPages() - 1) {
                paginatedPane.setPage(paginatedPane.getPage() + 1);

                gui.update();
            }
        }), 8, 0);
        gui.addPane(paginatedPane);
        gui.addPane(pagesPane);


        gui.show(player);
    }
}
