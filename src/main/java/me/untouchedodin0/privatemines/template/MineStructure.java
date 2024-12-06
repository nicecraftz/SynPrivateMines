package me.untouchedodin0.privatemines.template;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public record MineStructure(Vector spawn, BoundingBox mineArea, BoundingBox schematicArea) {
    public MineStructure shift(Vector vector) {
        return new MineStructure(
                spawn.clone().add(vector),
                mineArea.clone().shift(vector),
                schematicArea.clone().shift(vector)
        );
    }

    public Location getSpawnLocation(Location ref) {
        return spawn.toLocation(ref.getWorld()).clone();
    }

    @Override
    public Vector spawn() {
        return spawn.clone();
    }

    @Override
    public BoundingBox mineArea() {
        return mineArea.clone();
    }

    @Override
    public BoundingBox schematicArea() {
        return schematicArea.clone();
    }
}
