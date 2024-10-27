package me.untouchedodin0.privatemines.mine.task;

import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.scheduler.BukkitRunnable;

public class MineResetTask extends BukkitRunnable {
    private final MineService mineService;

    public MineResetTask(MineService mineService) {
        this.mineService = mineService;
    }

    @Override
    public void run() {
        for (Mine mine : mineService.getMines().values()) {
            if (mine.getAirPercentage() < mine.getMineData().getMineType().resetPercentage()) continue;
            mineService.reset(mine);
        }
    }
}
