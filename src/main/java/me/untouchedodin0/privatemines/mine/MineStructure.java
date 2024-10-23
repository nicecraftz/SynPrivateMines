package me.untouchedodin0.privatemines.mine;

import org.bukkit.Location;

public record MineStructure(
        Location max,
        Location min,
        Location minimumFullRegion,
        Location maximumFullRegion,
        Location mineLocation,
        Location spawnLocation
) {
}
