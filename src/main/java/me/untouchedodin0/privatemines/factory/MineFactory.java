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

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineCreationEvent;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.plugin.WorldGuardHook;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.utils.schematic.PasteHelper;
import me.untouchedodin0.privatemines.utils.schematic.PastedMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.UUID;

public class MineFactory {
    private final PrivateMines privateMines;
    private final PasteHelper pasteHelper;
    private final MineService mineService;

    private static final RegionOrchestrator regionOrchestrator = HookHandler.getHookHandler().get(
            WorldGuardHook.PLUGIN_NAME,
            WorldGuardHook.class
    ).getRegionOrchestrator();

    @ConfigurationEntry(key = "is-closed-by-default", section = "mine", type = ConfigurationValueType.BOOLEAN, value = "true")
    private boolean defaultClosed;

    public MineFactory(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.pasteHelper = privateMines.getPasteHelper();
        this.mineService = privateMines.getMineService();
        ConfigurationInstanceRegistry.registerInstance(this);
    }

    public Mine create(UUID uuid, Location location, MineType mineType) {
        File schematicFile = mineType.schematicFile();

        if (!schematicFile.exists()) {
            privateMines.logWarn("Schematic file does not exists: " + schematicFile.getName());
            return null;
        }

        String mineRegionName = String.format("mine-%s", uuid.toString());
        String fullRegionName = String.format("full-mine-%s", uuid.toString());

        PastedMine pastedMine = pasteHelper.paste(schematicFile, location);

        Location spawn = location.clone().add(0, 0, 1);
        BoundingBox mineArea = BoundingBox.of(pastedMine.getLowerRailsLocation(), pastedMine.getUpperRailsLocation());
        BoundingBox schematicArea = BoundingBox.of(pasteHelper.getMinimum(), pasteHelper.getMaximum());


        RegionOrchestrator regionOrchestrator = HookHandler.getHookHandler().get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator();

        regionOrchestrator.addRegion(fullRegionName, schematicArea);
        regionOrchestrator.addRegion(mineRegionName, mineArea);

        return createMineWithRegionConfiguration(uuid, mineType, mineArea, schematicArea, location, spawn);
    }


    private Mine createMineWithRegionConfiguration(UUID uuid, MineType mineType, BoundingBox mineArea, BoundingBox schematicArea, Location mineLocation, Location spawnLocation) {
        MineStructure mineStructure = new MineStructure(mineArea, schematicArea, mineLocation, spawnLocation);
        MineData mineData = new MineData(uuid, mineStructure, mineType);
        Mine mine = new Mine(mineData);
        mineData.setOpen(!defaultClosed);

        // todo: Handle this
//        SQLUtils.insert(mine);

        mineService.cache(mine);
        mineService.handleReset(mine);

        spawnLocation.getBlock().setType(Material.AIR);
        PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
        Bukkit.getPluginManager().callEvent(creationEvent);

        regionOrchestrator.setFlagsForMine(mine);
        return mine;
    }
}
