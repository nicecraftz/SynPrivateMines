package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.mine.Mine;

public interface RegionOrchestrator {
    String MINE_REGION_FORMAT = "mine-%s";
    String FULL_MINE_REGION_FORMAT = "full-mine-%s";

    void createMineFlaggedRegions(Mine mine);

    void removeMineRegions(Mine mine);

}
