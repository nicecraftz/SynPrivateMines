package me.untouchedodin0.privatemines.hook.plugin;

import com.fastasyncworldedit.core.extent.clipboard.CPUOptimizedClipboard;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockType;
import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.Entry;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.WorldIO;
import me.untouchedodin0.privatemines.storage.WeightedCollection;
import me.untouchedodin0.privatemines.template.SchematicPoints;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.NamespacedKey.minecraft;

public class WorldEditHook extends Hook {
    public static final String PLUGIN_NAME = "WorldEdit";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineService().getMinesWorld();

    private final List<BlockType> filter = Lists.newArrayList();

    private WorldEdit worldEdit;
    private WorldEditWorldIO worldEditWorldWriter;

    @Entry(key = "spawn-point", section = "materials", type = ConfigurationValueType.MATERIAL, value = "SPONGE")
    private Material spawnMaterial;

    @Entry(key = "mine-corner", section = "materials", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material cornerMaterial;

    @Entry(key = "sell-npc", section = "materials", type = ConfigurationValueType.MATERIAL, value = "WHITE_WOOL")
    private Material npcMaterial;

    @Entry(key = "quarry", section = "materials", type = ConfigurationValueType.MATERIAL, value = "SHULKER_BOX")
    private Material quarryMaterial;

    private BlockType spawnBlockType;
    private BlockType cornerBlockType;
    private BlockType npcBlockType;
    private BlockType quarryBlockType;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void hook() {
        ConfigurationInstanceRegistry.registerInstance(this);
        worldEdit = WorldEdit.getInstance();
        worldEditWorldWriter = new WorldEditWorldIO();
    }

    @Override
    public boolean required() {
        return true;
    }

    public WorldEditWorldIO getWorldEditWorldIO() {
        return worldEditWorldWriter;
    }

    public class WorldEditWorldIO implements WorldIO {

        @Override
        public void placeSchematic(File schematicFile, Location location) {
            com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(location.getWorld());

            try (
                    Clipboard clipboard = getClipboard(schematicFile); EditSession editSession = worldEdit.newEditSessionBuilder()
                    .world(worldEditWorld)
                    .fastMode(true)
                    .build()
            ) {
                clipboard.setOrigin(BlockVector3.ZERO);
                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(BukkitAdapter.asBlockVector(location))
                        .copyBiomes(false)
                        .copyEntities(false)
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
            }
        }

        @Override
        public void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials) {
            RandomPattern randomPattern = new RandomPattern();
            boundingBox.expand(-Math.abs(gap));

            for (Map.Entry<Double, String> materialDoubleEntry : materials.entrySet()) {
                Material material = Registry.MATERIAL.get(minecraft(materialDoubleEntry.getValue().toString()));
                if (material == null) continue;
                BlockData blockData = material.createBlockData();
                double chance = materialDoubleEntry.getKey();
                randomPattern.add(BukkitAdapter.adapt(blockData), chance);
            }

            try (
                    EditSession editSession = WorldEdit.getInstance()
                            .newEditSessionBuilder()
                            .world(BukkitAdapter.adapt(MINES_WORLD))
                            .fastMode(true)
                            .build()
            ) {
                BlockVector3 min = BukkitAdapter.asBlockVector(boundingBox.getMin().toLocation(MINES_WORLD));
                BlockVector3 max = BukkitAdapter.asBlockVector(boundingBox.getMax().toLocation(MINES_WORLD));
                Region region = new CuboidRegion(BukkitAdapter.adapt(MINES_WORLD), min, max);

                editSession.setBlocks(region, randomPattern);
            }
        }

        @Override
        public SchematicPoints computeSchematicPoints(File schematicFile) {
            setupBlockTypesAndFilter();

            Vector spawn = null;
            Vector mineCornerMin = null;
            Vector mineCornerMax = null;
            BoundingBox schematicArea;

            try (
                    Clipboard clipboard = getClipboard(schematicFile)
            ) {
                Region region = clipboard.getRegion();
                clipboard.setOrigin(BlockVector3.ZERO);

                BlockVector3 min = region.getMinimumPoint();
                BlockVector3 max = region.getMaximumPoint();
                schematicArea = BoundingBox.of(adapt(min), adapt(max));

                for (BlockVector3 currentVec : region) {
                    if (mineCornerMin != null && mineCornerMax != null && spawn != null) break;
                    BlockType blockType = clipboard.getBlock(currentVec).getBlockType();
                    if (!filter.contains(blockType)) continue;
                    if (blockType.equals(cornerBlockType) && (mineCornerMin == null || mineCornerMax == null)) {
                        if (mineCornerMin == null) mineCornerMin = adapt(currentVec);
                        else mineCornerMax = adapt(currentVec);
                    }
                    if (blockType.equals(spawnBlockType)) spawn = adapt(currentVec);
                }
            }

            if (spawn == null || mineCornerMin == null || mineCornerMax == null || schematicArea == null) {
                LoggerUtil.severe("Schematic %s is missing either spawn or corners.", schematicFile.getName());
                return null;
            }

            BoundingBox mineArea = BoundingBox.of(mineCornerMin, mineCornerMax);
            SchematicPoints schematicPoints = new SchematicPoints(spawn, mineArea, schematicArea);
            return schematicPoints;
        }

        private Vector adapt(BlockVector3 currentVec) {
            return BukkitAdapter.adapt(MINES_WORLD, currentVec).toVector();
        }

        private void setupBlockTypesAndFilter() {
            if (!filter.isEmpty()) return;
            spawnBlockType = BukkitAdapter.asBlockType(spawnMaterial);
            cornerBlockType = BukkitAdapter.asBlockType(cornerMaterial);
            npcBlockType = BukkitAdapter.asBlockType(npcMaterial);
            quarryBlockType = BukkitAdapter.asBlockType(quarryMaterial);
            filter.addAll(List.of(spawnBlockType, cornerBlockType, npcBlockType, quarryBlockType));
        }

        private Clipboard getClipboard(File schematicFile) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read(
                        UUID.randomUUID(),
                        // Thanks Pierre for the boost tip!
                        dimensions -> new CPUOptimizedClipboard(new CuboidRegion(
                                null,
                                BlockVector3.ZERO,
                                dimensions.subtract(BlockVector3.ONE),
                                false
                        ))
                );
                return clipboard;
            } catch (IOException e) {
                LoggerUtil.severe("There was an error while trying to load the worldedit clipboard", e);
                return null;
            }
        }
    }
}
