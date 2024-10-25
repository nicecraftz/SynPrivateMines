package me.untouchedodin0.privatemines.utils.schematic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.List;

public interface WorldWriter<C> {

    void placeSchematic(File schematicFile, Location location);

    C getClipboard(File schematicFile);

    void fill(World world, BoundingBox boundingBox, List<Material> materialList);
}
