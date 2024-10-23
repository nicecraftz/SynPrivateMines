package me.untouchedodin0.privatemines.utils.regions;

import me.untouchedodin0.privatemines.mine.Mine;

public interface RegionOrchestrator<R, F> {

    void setFlag(R region, F flag, boolean state);

    void setFlagsForMine(Mine mine);

}
