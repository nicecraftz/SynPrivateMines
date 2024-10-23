package me.untouchedodin0.privatemines.mine;

import org.bukkit.Location;

public record MineStructure(
        Location maxMineCorner,
        Location minMineCorner,
        Location mineFullMinCorner,
        Location mineFullMaxCorner,
        Location mineLocation,
        Location spawnLocation
) {
}
