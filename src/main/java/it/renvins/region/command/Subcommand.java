package it.renvins.region.command;

import org.bukkit.entity.Player;

public interface Subcommand {

    String getUsage();
    String getPermission();

    void execute(Player player, String[] args);
}
