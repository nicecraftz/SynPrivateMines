package me.untouchedodin0.privatemines.hook;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.Map;

public interface WorldWriter<C> {

    void placeSchematic(File schematicFile, Location location);

    C getClipboard(File schematicFile);

    void fill(BoundingBox boundingBox, Map<Material, Double> materials);

    void fillWithGap(BoundingBox boundingBox, int gap, Map<Material, Double> materials);
}
