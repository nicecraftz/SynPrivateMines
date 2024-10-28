package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.events.PrivateMineCreationEvent;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.hook.WorldIO;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import me.untouchedodin0.privatemines.hook.plugin.WorldGuardHook;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.UUID;

public class MineFactory {
    private final SchematicStorage schematicStorage;

    @ConfigurationEntry(key = "is-closed-by-default", section = "mine", type = ConfigurationValueType.BOOLEAN, value = "true")
    private boolean defaultClosed;

    public MineFactory(SchematicStorage schematicStorage) {
        ConfigurationInstanceRegistry.registerInstance(this);
        this.schematicStorage = schematicStorage;
    }

    public Mine create(UUID uuid, Location location, MineType mineType) {
        File schematicFile = mineType.schematicFile();

        if (!schematicFile.exists()) {
            LoggerUtil.warning("Schematic file does not exists: " + schematicFile.getName());
            return null;
        }

        WorldIO worldIO = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        BoundingBox schematicArea = worldIO.placeSchematic(schematicFile, location);
        Location spawn = location.clone().add(0, 0, 1);

        MineBlocks mineBlocks = schematicStorage.get(schematicFile);
        BoundingBox mineArea = mineBlocks.miningArea();
        MineStructure mineStructure = new MineStructure(mineArea, schematicArea, location, spawn);

        return createMineWithRegionConfiguration(uuid, mineType, mineStructure);
    }


    private Mine createMineWithRegionConfiguration(UUID uuid, MineType mineType, MineStructure mineStructure) {
        MineData mineData = new MineData(uuid, mineStructure, mineType);
        Mine mine = new Mine(mineData);
        mineData.setOpen(!defaultClosed);
        mineStructure.spawnLocation().getBlock().setType(Material.AIR);

        PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
        Bukkit.getPluginManager().callEvent(creationEvent);

        RegionOrchestrator regionOrchestrator = HookHandler.getHookHandler()
                .get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator();

        regionOrchestrator.createRegionsThenSetFlagsForMine(mine);
        return mine;
    }
}
