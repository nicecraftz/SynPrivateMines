package me.untouchedodin0.privatemines.template;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public record SchematicPoints(Vector spawn, BoundingBox mineArea, BoundingBox schematicArea) {


    public SchematicPoints shift(Vector vector) {
        return new SchematicPoints(
                spawn.clone().add(vector),
                mineArea.clone().shift(vector),
                schematicArea.clone().shift(vector)
        );
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
