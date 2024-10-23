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

package me.untouchedodin0.privatemines.factory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.untouchedodin0.kotlin.mine.storage.MineStorage;
import me.untouchedodin0.kotlin.mine.type.MineType;
import me.untouchedodin0.kotlin.utils.FlagUtils;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineCreationEvent;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineData;
import me.untouchedodin0.privatemines.mine.MineStructure;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.utils.worldedit.PasteHelper;
import me.untouchedodin0.privatemines.utils.worldedit.PastedMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import redempt.redlib.misc.Task;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MineFactory {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private static final MineStorage MINE_STORAGE = PRIVATE_MINES.getMineStorage();

    @ConfigurationEntry(key = "is-closed-by-default", section = "mine", type = ConfigurationValueType.BOOLEAN, value = "true")
    private boolean defaultClosed;

    public MineFactory() {
        InstanceRegistry.registerInstance(this);
    }

    /**
     * Creates a mine for the {@link Player} at {@link Location} with {@link MineType}
     *
     * @param player   the player the mine should be created forá
     * @param location the location of the mine
     * @param mineType the type of mine to paste
     */
    public void create(Player player, Location location, MineType mineType) {
        UUID uuid = player.getUniqueId();
        File schematicFile = new File("plugins/PrivateMines/schematics/" + mineType.getFile());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));

        if (!schematicFile.exists()) {
            PRIVATE_MINES.getLogger().warning("Schematic file does not exist: " + schematicFile.getName());
            return;
        }

        String mineRegionName = String.format("mine-%s", player.getUniqueId());
        String fullRegionName = String.format("full-mine-%s", player.getUniqueId());

        // todo: make class to handle worldedit functions to isolate it from all the code.
        Task.asyncDelayed(() -> {
            PasteHelper pasteHelper = new PasteHelper();
            PastedMine pastedMine = pasteHelper.paste(schematicFile, location);

            Location spawn = location.clone().add(0, 0, 1);
            Location corner1 = pastedMine.getLowerRailsLocation();
            Location corner2 = pastedMine.getUpperRailsLocation();
            Location minimum = pasteHelper.getMinimum();
            Location maximum = pasteHelper.getMaximum();
            BlockVector3 miningRegionMin = BukkitAdapter.asBlockVector(corner1);
            BlockVector3 miningRegionMax = BukkitAdapter.asBlockVector(corner2);
            BlockVector3 fullRegionMin = BukkitAdapter.asBlockVector(minimum);
            BlockVector3 fullRegionMax = BukkitAdapter.asBlockVector(maximum);

            ProtectedCuboidRegion fullRegion = new ProtectedCuboidRegion(fullRegionName, fullRegionMin, fullRegionMax);
            ProtectedCuboidRegion miningRegion = new ProtectedCuboidRegion(
                    mineRegionName,
                    miningRegionMin,
                    miningRegionMax
            );

            if (regionManager != null) {
                regionManager.addRegion(fullRegion);
                regionManager.addRegion(miningRegion);
            }

            MineStructure mineStructure = new MineStructure(corner2, corner1, minimum, maximum, location, spawn);
            MineData mineData = new MineData(uuid, mineStructure, mineType);
            Mine mine = new Mine(mineData);
            mineData.setOpen(!defaultClosed);

            SQLUtils.insert(mine);

            MINE_STORAGE.addMine(uuid, mine);
            mine.handleReset();

            Task.syncDelayed(() -> {
                spawn.getBlock().setType(Material.AIR);
                player.teleport(spawn);
                FlagUtils flagUtils = new FlagUtils();
                flagUtils.setFlags(mine);
                PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
                Bukkit.getPluginManager().callEvent(creationEvent);
            });
        });
    }

    public Mine createMine(Player player, Location location, MineType mineType) {
        UUID uuid = player.getUniqueId();
        File schematicFile = new File("plugins/PrivateMines/schematics/" + mineType.getFile());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));

        Map<Material, Double> materials = mineType.getMaterials();

        if (!schematicFile.exists()) {
            PRIVATE_MINES.getLogger().warning("Schematic file does not exist: " + schematicFile.getName());
            return null;
        }

        String mineRegionName = String.format("mine-%s", player.getUniqueId());
        String fullRegionName = String.format("full-mine-%s", player.getUniqueId());

        PasteHelper pasteHelper = new PasteHelper();
        PastedMine pastedMine = pasteHelper.paste(schematicFile, location);

        Location spawn = location.clone().add(0, 0, 1);
        Location corner1 = pastedMine.getLowerRailsLocation();
        Location corner2 = pastedMine.getUpperRailsLocation();
        Location minimum = pasteHelper.getMinimum();
        Location maximum = pasteHelper.getMaximum();
        BlockVector3 miningRegionMin = BukkitAdapter.asBlockVector(corner1);
        BlockVector3 miningRegionMax = BukkitAdapter.asBlockVector(corner2);
        BlockVector3 fullRegionMin = BukkitAdapter.asBlockVector(minimum);
        BlockVector3 fullRegionMax = BukkitAdapter.asBlockVector(maximum);

        ProtectedCuboidRegion miningRegion = new ProtectedCuboidRegion(
                mineRegionName,
                miningRegionMin,
                miningRegionMax
        );

        ProtectedCuboidRegion fullRegion = new ProtectedCuboidRegion(fullRegionName, fullRegionMin, fullRegionMax);

        if (regionManager != null) {
            regionManager.addRegion(miningRegion);
            regionManager.addRegion(fullRegion);
        }

        MineStructure mineStructure = new MineStructure(corner2, corner1, minimum, maximum, location, spawn);
        MineData mineData = new MineData(uuid, mineStructure, mineType);
        Mine mine = new Mine(mineData);
        mineData.setOpen(!defaultClosed);

        SQLUtils.insert(mine);
        MINE_STORAGE.addMine(uuid, mine);
        mine.handleReset();

        Task.syncDelayed(() -> {
            spawn.getBlock().setType(Material.AIR);
            player.teleport(spawn);
            FlagUtils flagUtils = new FlagUtils();
            flagUtils.setFlags(mine);

            PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
            Bukkit.getPluginManager().callEvent(creationEvent);
        });

        return mine;
    }

    public void createUpgraded(UUID uuid, Location location, MineType mineType) {
        MineStorage mineStorage = PRIVATE_MINES.getMineStorage();
        if (!mineStorage.hasMine(uuid)) return;

        Mine mine = createMine(Objects.requireNonNull(Bukkit.getPlayer(uuid)), location, mineType);
        mineStorage.replaceMine(uuid, mine);

        SQLUtils.replace(mine);

        PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
        Bukkit.getPluginManager().callEvent(creationEvent);
    }
}
