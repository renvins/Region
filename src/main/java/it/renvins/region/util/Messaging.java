package it.renvins.region.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Messaging {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String colorAndReplace(String message, Map<String, String> placeholders) {
        String colored = color(message);
        for (Map.Entry<String, String> entrySet : placeholders.entrySet()) {
            colored = colored.replaceAll(entrySet.getKey(), entrySet.getValue());
        }
        return colored;
    }

    public static List<String> colorAndReplaceList(List<String> messages, Map<String, String> placeholders) {
        return messages.stream().map(string -> Messaging.colorAndReplace(string, placeholders)).collect(Collectors.toList());
    }

    public static void sendMessage(CommandSender player, String message) {
        player.sendMessage(color(message));
    }
}
