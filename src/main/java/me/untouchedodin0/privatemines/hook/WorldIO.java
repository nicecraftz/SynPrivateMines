package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.utils.WeightedCollection;
import me.untouchedodin0.privatemines.template.MineStructure;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.io.File;

public interface WorldIO {
    WeightedCollection<String> EMPTY = WeightedCollection.single("air", 1);

    void placeSchematic(File schematicFile, Location location);

    void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials);

    MineStructure computeSchematicPoints(File schematicFile);

}
