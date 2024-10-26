package me.untouchedodin0.privatemines.mine;

import org.bukkit.Location;

public interface MineBlockHandler {
    String getPrefix();

    void place(String name, Location location);

    void remove(Location location);
}
