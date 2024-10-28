package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.MineBlocks;
import me.untouchedodin0.privatemines.mine.WeightedCollection;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.io.File;

public interface WorldIO {

    BoundingBox placeSchematic(File schematicFile, Location location);

    void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials);

    MineBlocks findRelativePoints(File schematicFile);

}
