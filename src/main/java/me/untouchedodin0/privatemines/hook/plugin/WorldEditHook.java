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
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.WorldIO;
import me.untouchedodin0.privatemines.mine.MineBlocks;
import me.untouchedodin0.privatemines.mine.WeightedCollection;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldEditHook extends Hook {
    public static final WeightedCollection<String> EMPTY = new WeightedCollection<>();
    public static final String PLUGIN_NAME = "WorldEdit";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();

    private final List<BlockType> filter = Lists.newArrayList();

    private WorldEdit worldEdit;
    private WorldEditWorldIO worldEditWorldWriter;

    @ConfigurationEntry(key = "spawn-point", section = "materials", type = ConfigurationValueType.MATERIAL, value = "SPONGE")
    private Material spawnMaterial;

    @ConfigurationEntry(key = "mine-corner", section = "materials", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material cornerMaterial;

    @ConfigurationEntry(key = "sell-npc", section = "materials", type = ConfigurationValueType.MATERIAL, value = "WHITE_WOOL")
    private Material npcMaterial;

    @ConfigurationEntry(key = "quarry", section = "materials", type = ConfigurationValueType.MATERIAL, value = "SHULKER_BOX")
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

    public WorldEditWorldIO getWorldEditWorldIO() {
        return worldEditWorldWriter;
    }

    public class WorldEditWorldIO implements WorldIO {

        @Override
        public BoundingBox placeSchematic(File schematicFile, Location location) {
            BlockVector3 adaptedLocation = BukkitAdapter.asBlockVector(location);
            com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(location.getWorld());

            try (
                    Clipboard clipboard = getClipboard(schematicFile); EditSession editSession = worldEdit.newEditSessionBuilder()
                    .world(worldEditWorld)
                    .fastMode(true)
                    .build()
            ) {
                Region region = clipboard.getRegion();
                Location min = BukkitAdapter.adapt(MINES_WORLD, region.getMinimumPoint());
                Location max = BukkitAdapter.adapt(MINES_WORLD, region.getMaximumPoint());
                BoundingBox boundingBox = BoundingBox.of(min, max);

                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(adaptedLocation)
                        .copyBiomes(false)
                        .copyEntities(false)
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);
                return boundingBox;

                // todo: remember to remove the mineblocks from the pasted schematic.
            }
        }

        @Override
        public void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials) {
            RandomPattern randomPattern = new RandomPattern();
            boundingBox.expand(-Math.abs(gap));

            for (Map.Entry<Double, String> materialDoubleEntry : materials.entrySet()) {
                Material material = Registry.MATERIAL.get(NamespacedKey.minecraft(materialDoubleEntry.getValue()
                        .toString()));
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
        public MineBlocks findRelativePoints(File schematicFile) {
            setupBlockTypesAndFilter();

            Vector spawnVec = null;
            Vector cornerMinVec = null;
            Vector cornerMaxVec = null;

            try (
                    Clipboard clipboard = getClipboard(schematicFile)
            ) {
                Region region = clipboard.getRegion();
                for (BlockVector3 currentVec : region) {
                    if (cornerMinVec != null && cornerMaxVec != null && spawnVec != null) break;
                    BlockType blockType = clipboard.getBlock(currentVec).getBlockType();
                    if (!filter.contains(blockType)) continue;
                    if (blockType.equals(cornerBlockType) && (cornerMinVec == null || cornerMaxVec == null)) {
                        if (cornerMinVec == null) cornerMinVec = fromBlockVector3(currentVec);
                        else cornerMaxVec = fromBlockVector3(currentVec);
                    }
                    if (blockType.equals(spawnBlockType)) spawnVec = fromBlockVector3(currentVec);
                }
            }

            if (spawnVec == null || cornerMinVec == null || cornerMaxVec == null) {
                LoggerUtil.severe("Schematic %s is missing either spawn or corners.", schematicFile.getName());
                return null;
            }

            BoundingBox mineArea = BoundingBox.of(cornerMinVec, cornerMaxVec);
            MineBlocks mineBlocks = new MineBlocks(spawnVec, mineArea);
            return mineBlocks;
        }

        private void setupBlockTypesAndFilter() {
            if (!filter.isEmpty()) return;
            spawnBlockType = BukkitAdapter.asBlockType(spawnMaterial);
            cornerBlockType = BukkitAdapter.asBlockType(cornerMaterial);
            npcBlockType = BukkitAdapter.asBlockType(npcMaterial);
            quarryBlockType = BukkitAdapter.asBlockType(quarryMaterial);
            filter.addAll(List.of(spawnBlockType, cornerBlockType, npcBlockType, quarryBlockType));
        }

        private Vector fromBlockVector3(BlockVector3 blockVector3) {
            return new Vector(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
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
