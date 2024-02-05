package it.renvins.region.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private Material material;

    private String name;

    private List<String> lore;
    private final Map<String, String> placeholders = new HashMap<>();

    private int amount = 1;

    private ItemBuilder material(String material) {
        this.material = Material.getMaterial(material);
        return this;
    }

    private ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    private ItemBuilder lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    private ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    private ItemBuilder addPlaceholder(String key, String value) {
        placeholders.put(key, value);
        return this;
    }

    private ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();

        if(name != null) {
            itemMeta.setDisplayName(Messaging.colorAndReplace(name, placeholders));
        }
        if(lore != null) {
            itemMeta.setLore(Messaging.colorAndReplaceList(lore, placeholders));
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack createItem(ConfigurationSection section, String itemName, Map<String, String> placeholders) {
        ItemBuilder itemBuilder = new ItemBuilder();

        String material = section.getString(itemName + ".material");
        itemBuilder.material(material);

        if(section.contains(itemName + ".name")) {
            itemBuilder.name(section.getString(itemName + ".name"));
        }
        if(section.contains(itemName + ".lore")) {
            itemBuilder.lore(section.getStringList(itemName + ".lore"));
        }

        if(placeholders != null) {
            for(Map.Entry<String, String> entry : placeholders.entrySet()) {
                itemBuilder.addPlaceholder(entry.getKey(), entry.getValue());
            }
        }

        return itemBuilder.build();
    }
}
