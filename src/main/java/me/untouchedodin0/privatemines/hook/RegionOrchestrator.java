package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.util.BoundingBox;

public interface RegionOrchestrator {

    void setFlag(String region, String flag, boolean state);

    void setFlagsForMine(Mine mine);

    void removeMineRegions(Mine mine);

    void removeRegion(String name);

    void addRegion(String name, BoundingBox boundingBox);
}
