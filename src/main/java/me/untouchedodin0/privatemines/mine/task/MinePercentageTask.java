package me.untouchedodin0.privatemines.mine.task;

import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class MinePercentageTask extends BukkitRunnable {
    private final MineService mineService;

    public MinePercentageTask(MineService mineService) {
        this.mineService = mineService;
    }

    @Override
    public void run() {
        for (Mine mine : mineService.getMines().values()) {
            BoundingBox boundingBox = mine.getSchematicPoints().mineArea();
            int totalBlocks = 0;
            for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
                for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                    for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                        Location location = new Location(mineService.getMinesWorld(), x, y, z);
                        if (location.getBlock().getType().isAir()) totalBlocks++;
                    }
                }
            }
            mine.setAirPercentage((double) totalBlocks / boundingBox.getVolume() * 100);
        }
    }
}
