package me.untouchedodin0.privatemines.hook;

import org.bukkit.Location;

public interface MineBlockHandler {
    String getPrefix();

    void place(String name, Location location);

    void remove(Location location);

    // todo: implement this feature to support multiple blocks of different type in the mine.
//    void resetMine(Mine mine);
}
