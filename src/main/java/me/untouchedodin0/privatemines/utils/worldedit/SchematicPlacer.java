package me.untouchedodin0.privatemines.utils.worldedit;

import org.bukkit.Location;

import java.io.File;

public interface SchematicPlacer<C> {

    void placeSchematic(File schematicFile, Location location);

    C getClipboard(File schematicFile);
}
