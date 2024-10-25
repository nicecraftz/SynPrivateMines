package me.untouchedodin0.privatemines.utils.regions;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.util.BoundingBox;

public interface RegionOrchestrator<R, F> {

    void setFlag(R region, F flag, boolean state);

    void setFlagsForMine(Mine mine);

    void removeRegion(String name);

    void addRegion(String name, BoundingBox boundingBox);
}
