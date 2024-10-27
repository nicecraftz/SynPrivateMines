package me.untouchedodin0.privatemines.mine.task;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.mine.MineData;
import me.untouchedodin0.privatemines.mine.MineStructure;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class MinePercentageTask extends BukkitRunnable {
    private final MineData mineData;
    private double airPercentage = 0;

    public MinePercentageTask(MineData mineData) {
        this.mineData = mineData;
    }

    @Override
    public void run() {
        MineStructure mineStructure = mineData.getMineStructure();
        BoundingBox boundingBox = mineStructure.mineBoundingBox();

        int totalBlocks = 0;
        for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                    Location location = new Location(PrivateMines.getInstance().getMineWorldManager().getMinesWorld(),
                            x,
                            y,
                            z
                    );
                    if (location.getBlock().getType().isAir()) totalBlocks++;
                }
            }
        }

        airPercentage = (double) totalBlocks / boundingBox.getVolume() * 100;
    }

    public double getAirPercentage() {
        return airPercentage;
    }
}
