package me.untouchedodin0.privatemines.mine;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public record MineStructure(
        BoundingBox mineBoundingBox,
        BoundingBox schematicBoundingBox,
        Location mineLocation,
        Location spawnLocation
) {
}
