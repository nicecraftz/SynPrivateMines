package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.util.BoundingBox;

public interface RegionOrchestrator {
    
    void createRegionsThenSetFlagsForMine(Mine mine);

    void removeMineRegions(Mine mine);

    void addRegion(String name, BoundingBox boundingBox);
}
