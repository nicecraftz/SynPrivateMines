package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.WeightedCollection;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;

public interface WorldIO {

    BoundingBox placeSchematic(File schematicFile, Location location);

    void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials);

    MineBlocks findRelativePoints(File schematicFile);


    record MineBlocks(Vector spawnLocation, BoundingBox miningArea, Vector npcLocation, Vector quarryLocation) {
        public MineBlocks {
            if (spawnLocation == null) throw new IllegalArgumentException("Spawn location cannot be null");
            if (miningArea == null) throw new IllegalArgumentException("Area cannot be null");
        }

        public boolean hasNpcLocation() {
            return npcLocation != null;
        }

        public boolean hasQuarryLocation() {
            return quarryLocation != null;
        }
    }

}
