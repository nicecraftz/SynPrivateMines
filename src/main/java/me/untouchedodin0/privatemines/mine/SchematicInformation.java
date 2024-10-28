package me.untouchedodin0.privatemines.mine;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public record SchematicInformation(Vector spawnLocation, BoundingBox miningArea, BoundingBox schematicBoundingBox) {
    public SchematicInformation {
        if (spawnLocation == null) throw new IllegalArgumentException("Spawn location cannot be null");
        if (miningArea == null) throw new IllegalArgumentException("Area cannot be null");
    }

    @Override
    public BoundingBox miningArea() {
        return miningArea.clone();
    }

    @Override
    public Vector spawnLocation() {
        return spawnLocation.clone();
    }

    @Override
    public BoundingBox schematicBoundingBox() {
        return schematicBoundingBox.clone();
    }
}

