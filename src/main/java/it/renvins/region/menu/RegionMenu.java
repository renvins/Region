package it.renvins.region.menu;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import it.renvins.region.RegionLoader;
import it.renvins.region.conversation.RenamePrompt;
import it.renvins.region.conversation.WhitelistAddPrompt;
import it.renvins.region.conversation.WhitelistRemovePrompt;
import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.ItemBuilder;
import it.renvins.region.util.Messaging;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RegionMenu {

    private final IConfigService configService;
    private final IRegionsService regionsService;

    private final Region region;
    private final Player player;

    private final ConfigurationSection guiSection;
    private final ChestGui gui;

    public RegionMenu(IConfigService configService, IRegionsService regionsService, Region region, Player player) {
        this.configService = configService;
        this.regionsService = regionsService;

        this.region = region;
        this.player = player;

        this.guiSection = configService.getConfig().getConfigurationSection("regionMenu");
        gui = new ChestGui(1,
                Messaging.color(guiSection.getString("title")));

    }

    public void init() {
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        StaticPane staticPane = new StaticPane(0, 0, 9, 1);
        ConfigurationSection itemsSection = guiSection.getConfigurationSection("items");

        GuiItem renameItem = new GuiItem(ItemBuilder.createItem(itemsSection, "renameItem", null),
                event -> initConversation(new RenamePrompt(region.getName(), configService, regionsService)));
        GuiItem whitelistAddItem = new GuiItem(ItemBuilder.createItem(itemsSection, "whitelistAddItem", null),
                event -> initConversation(new WhitelistAddPrompt(region, configService, regionsService)));
        GuiItem whitelistRemoveItem = new GuiItem(ItemBuilder.createItem(itemsSection, "whitelistRemoveItem", null),
                event -> initConversation(new WhitelistRemovePrompt(region, configService, regionsService)));

        GuiItem redefineRegion = new GuiItem(ItemBuilder.createItem(itemsSection, "redefineItem", null),
                event -> {
                    ItemStack item = ItemBuilder.createItem(configService.getConfig().getConfigurationSection("setup"), "wand", null);
                    player.getInventory().addItem(item);

                    Messaging.sendMessage(player, configService.getLang().getString("wandGiven"));
                    regionsService.addToRedefining(player, region);
        });

        staticPane.addItem(renameItem, 0, 0);
        staticPane.addItem(whitelistAddItem, 1, 0);
        staticPane.addItem(whitelistRemoveItem, 2, 0);
        staticPane.addItem(redefineRegion, 3, 0);

        gui.addPane(staticPane);
        gui.show(player);
    }

    private void initConversation(StringPrompt prompt) {
        player.closeInventory();
        RegionLoader.getConversationFactory().withFirstPrompt(prompt).withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage(configService.getLang().getString("onlyPlayers")).buildConversation(player).begin();
    }
}
