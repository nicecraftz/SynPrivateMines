package me.untouchedodin0.privatemines.mine.factory;

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
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.utils.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

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
        LoggerUtil.info("Free location is at %s", SerializationUtil.locationToString(location));
        File schematicFile = mineType.schematicFile();

        if (!schematicFile.exists()) {
            LoggerUtil.warning("Schematic file does not exists: " + schematicFile.getName());
            return null;
        }

        WorldIO worldIO = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        BoundingBox schematicArea = worldIO.placeSchematic(schematicFile, location);

        SchematicInformation schematicInformation = schematicStorage.get(schematicFile);
        BoundingBox mineArea = schematicInformation.miningArea().shift(location.toVector());
        Vector spawnLocation = schematicInformation.spawnLocation().add(location.toVector());

        MineStructure mineStructure = new MineStructure(
                mineArea,
                schematicArea,
                spawnLocation.toLocation(location.getWorld())
        );

        MineData mineData = new MineData(uuid, mineStructure, mineType);

        Mine mine = new Mine(mineData);
        mineData.setOpen(!defaultClosed);

        Block spawnBlock = mineStructure.spawnLocation().getBlock();
        spawnBlock.setType(Material.AIR);

        PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
        Bukkit.getPluginManager().callEvent(creationEvent);

        RegionOrchestrator regionOrchestrator = HookHandler.getHookHandler()
                .get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator();

        regionOrchestrator.createRegionsThenSetFlagsForMine(mine);
        return mine;
    }
}
