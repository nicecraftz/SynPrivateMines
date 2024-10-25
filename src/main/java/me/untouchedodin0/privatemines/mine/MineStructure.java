package me.untouchedodin0.privatemines.mine;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public record MineStructure(
        Location maxMineCorner,
        Location minMineCorner,
        Location schematicMinCorner,
        Location schematicMaxCorner,
        Location mineLocation,
        Location spawnLocation
) {

    public BoundingBox mineBoundingBox() {
        return BoundingBox.of(minMineCorner, maxMineCorner);
    }

    public BoundingBox schematicBoundingBox() {
        return BoundingBox.of(schematicMinCorner, schematicMaxCorner);
    }

    public static MineStructure fromBoundingBoxes(World world, BoundingBox mineArea, BoundingBox schematicArea, Location mineLocation, Location spawnLocation) {
        return new MineStructure(
                mineArea.getMax().toLocation(world),
                mineArea.getMin().toLocation(world),
                schematicArea.getMin().toLocation(world),
                schematicArea.getMax().toLocation(world),
                mineLocation,
                spawnLocation
        );
    }

}
