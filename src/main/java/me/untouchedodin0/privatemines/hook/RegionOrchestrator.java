package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.util.BoundingBox;

public interface RegionOrchestrator<R, F> {

    void setFlag(R region, F flag, boolean state);

    void setFlagsForMine(Mine mine);

    void removeMineRegions(Mine mine);

    void removeRegion(String name);

    void addRegion(String name, BoundingBox boundingBox);
}
