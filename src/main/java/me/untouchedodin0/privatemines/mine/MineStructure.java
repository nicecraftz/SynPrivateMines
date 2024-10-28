package me.untouchedodin0.privatemines.mine;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;


// todo: deprecate in favor of SchematicInformation
@Deprecated(forRemoval = true, since = "1.0.0")
public record MineStructure(
        BoundingBox mineBoundingBox,
        BoundingBox schematicBoundingBox,
        Location spawnLocation
) {


    @Override
    public BoundingBox mineBoundingBox() {
        return mineBoundingBox.clone();
    }

    @Override
    public BoundingBox schematicBoundingBox() {
        return schematicBoundingBox.clone();
    }

    @Override
    public Location spawnLocation() {
        return spawnLocation.clone();
    }
}
