package me.untouchedodin0.privatemines.hook;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Map;

public interface MineBlockHandler {
    String getPrefix();

    void place(String name, Location location);

    void remove(Location location);

    void removeFromArea(BoundingBox boundingBox);

    void placeInAreaWithChance(Map<String, Double> blocks, BoundingBox boundingBox);
}
