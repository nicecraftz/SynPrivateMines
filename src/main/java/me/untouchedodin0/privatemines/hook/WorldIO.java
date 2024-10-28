package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.SchematicInformation;
import me.untouchedodin0.privatemines.storage.WeightedCollection;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.io.File;

public interface WorldIO {

    BoundingBox placeSchematic(File schematicFile, Location location);

    void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials);

    SchematicInformation findRelativePoints(File schematicFile);

}
