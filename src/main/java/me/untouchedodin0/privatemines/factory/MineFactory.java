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
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineCreationEvent;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.hook.WorldguardHook;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.utils.schematic.PasteHelper;
import me.untouchedodin0.privatemines.utils.schematic.PastedMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.UUID;

public class MineFactory {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private static final PasteHelper PASTE_HELPER = PRIVATE_MINES.getPasteHelper();
    private static final MineService MINE_SERVICE = PRIVATE_MINES.getMineService();

    private static final RegionOrchestrator regionOrchestrator = HookHandler.get(WorldguardHook.PLUGIN_NAME,
            WorldguardHook.class
    ).getRegionOrchestrator();

    @ConfigurationEntry(key = "is-closed-by-default", section = "mine", type = ConfigurationValueType.BOOLEAN, value = "true")
    private boolean defaultClosed;

    public MineFactory() {
        InstanceRegistry.registerInstance(this);
    }

    public Mine create(UUID uuid, Location location, MineType mineType) {
        File schematicFile = mineType.schematicFile();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));

        if (!schematicFile.exists()) {
            PRIVATE_MINES.getLogger().warning("Schematic file does not exist: " + schematicFile.getName());
            return null;
        }

        String mineRegionName = String.format("mine-%s", uuid.toString());
        String fullRegionName = String.format("full-mine-%s", uuid.toString());

        PastedMine pastedMine = PASTE_HELPER.paste(schematicFile, location);

        Location spawn = location.clone().add(0, 0, 1);
        BoundingBox mineArea = BoundingBox.of(pastedMine.getLowerRailsLocation(), pastedMine.getUpperRailsLocation());
        BoundingBox schematicArea = BoundingBox.of(PASTE_HELPER.getMinimum(), PASTE_HELPER.getMaximum());


        RegionOrchestrator regionOrchestrator = HookHandler.get(WorldguardHook.PLUGIN_NAME, WorldguardHook.class)
                .getRegionOrchestrator();

        regionOrchestrator.addRegion(fullRegionName, schematicArea);
        regionOrchestrator.addRegion(mineRegionName, mineArea);

        return createMineWithRegionConfiguration(uuid, mineType, mineArea, schematicArea, location, spawn);
    }


    private Mine createMineWithRegionConfiguration(UUID uuid, MineType mineType, BoundingBox mineArea, BoundingBox schematicArea, Location mineLocation, Location spawnLocation) {
        MineStructure mineStructure = MineStructure.fromBoundingBoxes(PRIVATE_MINES.getMineWorldManager()
                .getMinesWorld(), mineArea, schematicArea, mineLocation, spawnLocation);
        MineData mineData = new MineData(uuid, mineStructure, mineType);
        Mine mine = new Mine(mineData);
        mineData.setOpen(!defaultClosed);

        SQLUtils.insert(mine);
        MINE_SERVICE.cache(mine);
        mine.handleReset();

        spawnLocation.getBlock().setType(Material.AIR);
        PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
        Bukkit.getPluginManager().callEvent(creationEvent);

        regionOrchestrator.setFlagsForMine(mine);
        return mine;
    }
}
