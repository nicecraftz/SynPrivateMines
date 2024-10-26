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
import me.untouchedodin0.privatemines.events.PrivateMineExpandEvent;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Mine {
    private final MineData mineData;
    private boolean canExpand = true;
    private int airBlocks;

    public Mine(MineData mineData) {
        this.mineData = mineData;
    }

    public MineData getMineData() {
        return mineData;
    }

    public void teleport(Entity entity) {
        Location spawnLocation = mineData.getMineStructure().spawnLocation();
        Block block = spawnLocation.getBlock();
        if (!block.getType().isBlock()) return;
        block.setType(Material.AIR, false);
        entity.teleportAsync(spawnLocation);
    }

    public double getPercentage() {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(
                mineData.getMinimumMining().getBlockX(),
                mineData.getMinimumMining().getBlockY(),
                mineData.getMinimumMining().getBlockZ()
        ), BlockVector3.at(
                mineData.getMaximumMining().getBlockX(),
                mineData.getMaximumMining().getBlockY(),
                mineData.getMaximumMining().getBlockZ()
        ));

        if (Config.addWallGap) {
            for (int i = 0; i < Config.wallsGap; i++) {
                region.contract(ExpansionUtils.contractVectors(1));
            }
        }

        long total = region.getVolume();

        // Calculate the percetage of the region called "region" to then compare with how many blocks have been mined.
        airBlocks = 0;
        for (BlockVector3 vector : region) {
            Block block = Objects.requireNonNull(Bukkit.getWorld(Objects.requireNonNull(Objects.requireNonNull(
                            getSpawnLocation()).getWorld()).getName()))
                    .getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            if (block.getType().equals(Material.AIR)) {
                this.airBlocks++;
            }
        }
        return (float) airBlocks * 100L / total;
    }

    public void handleReset() {
        MineData mineData = getMineData();
        MineType mineType = mineData.getMineType();

        boolean useOraxen = mineType.getUseOraxen();
        boolean useItemsAdder = mineType.getUseItemsAdder();

        if (!useOraxen && !useItemsAdder) {
            reset();
        } else if (mineType.getOraxen() != null && useOraxen) {
            resetOraxen();
        } else if (mineType.getItemsAdder() != null && useItemsAdder) {
            resetItemsAdder();
        }

        World world = PrivateMines.getInstance().getMineWorldManager().getMinesWorld();
        BlockVector3 corner1 = BukkitAdapter.asBlockVector(mineData.getMinimumMining());
        BlockVector3 corner2 = BukkitAdapter.asBlockVector(mineData.getMaximumMining());

        Region region = new CuboidRegion(BukkitAdapter.adapt(world), corner1, corner2);
        Player owner = Bukkit.getPlayer(mineData.getMineOwner());

        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean isPlayerInRegion = region.contains(
                    online.getLocation().getBlockX(),
                    online.getLocation().getBlockY(),
                    online.getLocation().getBlockZ()
            );
            boolean inWorld = online.getWorld().equals(world);

            if (isPlayerInRegion && inWorld) {
                Task.syncDelayed(() -> teleport(online));
            }
        }
        if (owner != null) {
            boolean isPlayerInRegion = region.contains(
                    owner.getLocation().getBlockX(),
                    owner.getLocation().getBlockY(),
                    owner.getLocation().getBlockZ()
            );
            boolean inWorld = owner.getWorld().equals(world);
            if (isPlayerInRegion && inWorld) {
                Task.syncDelayed(() -> teleport(owner));
            }
        }

        if (percentageTask == null) {
            //Create a new Bukkit task async
            this.percentageTask = Task.syncRepeating(() -> {
                double percentage = getPercentage();
                double resetPercentage = mineType.getResetPercentage();
                if (percentage > resetPercentage) {
                    handleReset();
                    airBlocks = 0;
                }
            }, 0, 80);
        }
        if (owner != null) {
            audienceUtils.sendMessage(owner, MessagesConfig.mineReset);
        }
    }

    public void startTasks() {
        MineType mineType = mineData.getMineType();

        if (task == null) {
            this.task = Task.syncRepeating(this::handleReset, 0L, mineType.getResetTime() * 20L);
        }
    }

    public void stopTasks() {
        if (task != null) {
            task.cancel();
        }
        if (percentageTask != null) {
            percentageTask.cancel();
        }
        privateMines.getLogger().info("Stopped tasks for mine " + mineData.getMineOwner());
    }


    public boolean isBanned(UUID uuid) {
        return mineData.isBanned(uuid);
    }

    public void ban(UUID uuid) {
        mineData.addBannedPlayer(uuid);
        SQLUtils.update(this);
    }

    public void unban(UUID uuid) {
        mineData.removeBannedPlayer(uuid);
        SQLUtils.update(this);
    }

    public boolean canExpand(final int amount) {
        final World world = privateMines.getMineWorldManager().getMinesWorld();
        final var min = getMineData().getMinimumMining();
        final var max = getMineData().getMaximumMining();
        final var region = new CuboidRegion(
                BukkitAdapter.asBlockVector(min),
                BukkitAdapter.asBlockVector(max)
        );
        final boolean borderUpgrade = Config.borderUpgrade;

        region.expand(ExpansionUtils.expansionVectors(amount + 1));
        region.forEach(blockVector3 -> {
            Material type = Utils.toLocation(blockVector3, world).getBlock().getType();
            if (type.equals(Config.upgradeMaterial)) {
                canExpand = false;
                if (borderUpgrade) {
                    upgrade();
                }
            }
        });
        return canExpand;
    }

    public void expand(int amount) {
        final World world = privateMines.getMineWorldManager().getMinesWorld();
        boolean canExpand = canExpand(1);

        if (!canExpand) {
            privateMines.getLogger().info("Failed to expand the mine due to the mine being too large");
        } else {
            final var fillType = BlockTypes.DIAMOND_BLOCK;
            final var wallType = BlockTypes.BEDROCK;
            final var min = getMineData().getMinimumMining();
            final var max = getMineData().getMaximumMining();

            final Region mineRegion = new CuboidRegion(
                    BukkitAdapter.asBlockVector(min),
                    BukkitAdapter.asBlockVector(max)
            );

            final Region airFillRegion = new CuboidRegion(
                    BukkitAdapter.asBlockVector(min),
                    BukkitAdapter.asBlockVector(max)
            );

            final Region wallsRegion = new CuboidRegion(
                    BukkitAdapter.asBlockVector(min),
                    BukkitAdapter.asBlockVector(max)
            );

            if (fillType == null || wallType == null) {
                return;
            }

            mineRegion.expand(ExpansionUtils.expansionVectors(1));
            wallsRegion.expand(ExpansionUtils.expansionVectors(1));

            if (Config.shouldWallsGoUp) {
                wallsRegion.expand(
                        BlockVector3.UNIT_X,
                        BlockVector3.UNIT_Y,
                        BlockVector3.UNIT_Z,
                        BlockVector3.UNIT_MINUS_X,
                        BlockVector3.UNIT_MINUS_Y,
                        BlockVector3.UNIT_MINUS_Z
                );
            } else {
                wallsRegion.expand(
                        BlockVector3.UNIT_X,
                        BlockVector3.UNIT_Z,
                        BlockVector3.UNIT_MINUS_X,
                        BlockVector3.UNIT_MINUS_Y,
                        BlockVector3.UNIT_MINUS_Z
                );
            }

            airFillRegion.expand(
                    BlockVector3.UNIT_X,
                    BlockVector3.UNIT_Y,
                    BlockVector3.UNIT_Z,
                    BlockVector3.UNIT_MINUS_X,
                    BlockVector3.UNIT_MINUS_Z
            );

            Map<Material, Double> materials = mineData.getMineType().getMaterials();
            final RandomPattern randomPattern = new RandomPattern();

            if (materials != null) {
                materials.forEach((material, chance) -> {
                    Pattern pattern = BukkitAdapter.adapt(material.createBlockData());
                    randomPattern.add(pattern, chance);
                });
            }

            PrivateMineExpandEvent privateMineExpandEvent = new PrivateMineExpandEvent(
                    this,
                    PrivateMineExpandEvent.MineExpandEventRegion.fromWorldguardRegion(mineRegion)
            );

            Task.syncDelayed(() -> Bukkit.getPluginManager().callEvent(privateMineExpandEvent));
            if (privateMineExpandEvent.isCancelled()) {
                return;
            }

            try (
                    EditSession editSession = WorldEdit.getInstance()
                            .newEditSessionBuilder()
                            .world(BukkitAdapter.adapt(world))
                            .fastMode(true)
                            .build()
            ) {
                editSession.setBlocks(wallsRegion, BukkitAdapter.adapt(Material.BEDROCK.createBlockData()));
                editSession.setBlocks(airFillRegion, BukkitAdapter.adapt(Material.AIR.createBlockData()));
            }

            mineData.setMinimumMining(BukkitAdapter.adapt(world, mineRegion.getMinimumPoint()));
            mineData.setMaximumMining(BukkitAdapter.adapt(world, mineRegion.getMaximumPoint()));
            mineData.setMinimumFullRegion(mineData.getMinimumFullRegion().subtract(1, 1, 1));
            mineData.setMaximumFullRegion(mineData.getMaximumFullRegion().add(1, 1, 1));
            String mineRegionName = String.format("mineRegion-%s", mineData.getMineOwner());

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

            if (regionManager != null) {
                regionManager.removeRegion(mineRegionName);
            }

            ProtectedCuboidRegion protectedCuboidRegion = new ProtectedCuboidRegion(
                    mineRegionName,
                    mineRegion.getMinimumPoint(),
                    mineRegion.getMaximumPoint()
            );
            if (regionManager != null) {
                regionManager.addRegion(protectedCuboidRegion);
            }

            FlagUtils flagUtils = new FlagUtils();
            flagUtils.setFlags(this);

            setMineData(mineData);
            privateMines.getMineStorage().replaceMineNoLog(mineData.getMineOwner(), this);
            handleReset();
            SQLUtils.update(this);
        }
        this.canExpand = true;
    }

    public @NotNull Location getMineLocation() {
        return mineData.getMineStructure().mineLocation();
    }
}

