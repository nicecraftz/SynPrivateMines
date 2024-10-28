package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.privatemines.mine.task.MinePercentageTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Mine {
    private final MineData mineData;
    private final MinePercentageTask percentageTask;

    public Mine(MineData mineData) {
        this.mineData = mineData;
        percentageTask = new MinePercentageTask(mineData);
    }

    public MineData getMineData() {
        return mineData;
    }

    public int getAirPercentage() {
        return (int) percentageTask.getAirPercentage();
    }

    public void teleport(Entity entity) {
        Location spawnLocation = mineData.getMineStructure().spawnLocation();
        Block block = spawnLocation.getBlock();
        if (!block.getType().isBlock()) return;
        block.setType(Material.AIR, false);
        entity.teleportAsync(spawnLocation);
    }

    public boolean isBanned(UUID uuid) {
        return mineData.isBanned(uuid);
    }

    public void ban(UUID uuid) {
        mineData.addBannedPlayer(uuid);
//        SQLUtils.update(this);
    }

    public void unban(UUID uuid) {
        mineData.removeBannedPlayer(uuid);
//        SQLUtils.update(this);
    }

    public @NotNull Location mineSpawnLocation() {
        return mineData.getMineStructure().spawnLocation();
    }
}

