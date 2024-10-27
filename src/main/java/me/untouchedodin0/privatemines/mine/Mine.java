/**
 * MIT License
 * <p>
 * Copyright (c) 2021 - 2023 Kyle Hicks
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.mine.task.MinePercentageTask;
import org.bukkit.Bukkit;
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

    public void startTasks() {
        MineType mineType = mineData.getMineType();
        Bukkit.getScheduler().runTaskTimer(PrivateMines.getInstance(), task, 0L, mineType.resetTime() * 20L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(PrivateMines.getInstance(), percentageTask, 0L, 20L * 10);
    }

    public void stopTasks() {
        task.cancel();
        // todo: recreate percentageTask
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

    public Location getSpawnLocation() {
        return mineData.getMineStructure().spawnLocation();
    }

    public @NotNull Location getMineLocation() {
        return mineData.getMineStructure().mineLocation();
    }
}

