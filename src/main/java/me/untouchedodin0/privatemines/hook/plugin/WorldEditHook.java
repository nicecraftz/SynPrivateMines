package me.untouchedodin0.privatemines.hook.plugin;

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
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.WorldIO;
import me.untouchedodin0.privatemines.mine.WeightedCollection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;

public class WorldEditHook extends Hook {
    public static final WeightedCollection EMPTY = new WeightedCollection<>();
    public static final String PLUGIN_NAME = "WorldEdit";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();

    private WorldEdit worldEdit;
    private WorldEditWorldIO worldEditWorldWriter;

    @ConfigurationEntry(key = "spawn-point", section = "materialChance", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material spawnMaterial;

    @ConfigurationEntry(key = "mine-corner", section = "materialChance", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material cornerMaterial;

    @ConfigurationEntry(key = "sell-npc", section = "materialChance", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material npcMaterial;

    @ConfigurationEntry(key = "quarry", section = "materialChance", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material quarryMaterial;


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

    public WorldEditWorldIO getWorldEditWorldWriter() {
        return worldEditWorldWriter;
    }

    public class WorldEditWorldIO implements WorldIO {

        @Override
        public BoundingBox placeSchematic(File schematicFile, Location location) {
            BlockVector3 adaptedLocation = BukkitAdapter.asBlockVector(location);
            com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(location.getWorld());

            try (
                    Clipboard clipboard = getClipboard(schematicFile);
                    EditSession editSession = worldEdit.newEditSession(worldEditWorld)
            ) {
                Region region = clipboard.getRegion();
                BlockVector3 minimumPoint = region.getMinimumPoint();
                BlockVector3 maximumPoint = region.getMaximumPoint();

                BoundingBox boundingBox = BoundingBox.of(
                        new Vector(minimumPoint.getX(), minimumPoint.getY(), minimumPoint.getZ()),
                        new Vector(maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ())
                );
                
                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(adaptedLocation)
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);
                return boundingBox;
            }
        }

        @Override
        public void fill(BoundingBox boundingBox, int gap, WeightedCollection<String> materials) {
            RandomPattern randomPattern = new RandomPattern();
            boundingBox.expand(-Math.abs(gap));

            for (Map.Entry<Material, Double> materialDoubleEntry : materials.entrySet()) {
                BlockData blockData = materialDoubleEntry.getKey().createBlockData();
                double chance = materialDoubleEntry.getValue();
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
            BlockVector3 spawnBlockVector3 = null;
            BlockVector3 npcBlockVector3 = null;
            BlockVector3 quarryBlockVector3 = null;
            BlockVector3 cornerMinBlockVector3 = null;
            BlockVector3 cornerMaxBlockVector3 = null;

            try (
                    Clipboard clipboard = getClipboard(schematicFile)
            ) {
                Region region = clipboard.getRegion();

                for (BlockVector3 currentIterativeBlockVector : region) {
                    BlockType blockType = clipboard.getBlock(currentIterativeBlockVector).getBlockType();
                    Material bukkitMaterial = adapt(blockType);

                    if (bukkitMaterial == cornerMaterial && (cornerMinBlockVector3 == null || cornerMaxBlockVector3 == null)) {
                        if (cornerMinBlockVector3 == null) {
                            cornerMinBlockVector3 = currentIterativeBlockVector;
                        } else if (cornerMaxBlockVector3 == null) {
                            cornerMaxBlockVector3 = currentIterativeBlockVector;
                        }
                        continue;
                    }

                    if (bukkitMaterial == spawnMaterial) {
                        spawnBlockVector3 = currentIterativeBlockVector;
                        continue;
                    }

                    if (bukkitMaterial == npcMaterial) {
                        npcBlockVector3 = currentIterativeBlockVector;
                        continue;
                    }

                    if (bukkitMaterial == quarryMaterial) {
                        quarryBlockVector3 = currentIterativeBlockVector;
                    }
                }
            }

            if (spawnBlockVector3 == null || cornerMinBlockVector3 == null || cornerMaxBlockVector3 == null) {
                PLUGIN_INSTANCE.logInfo("Please, make sure you have placed the spawn and corner blocks.");
                return null;
            }

            BlockVector3[] corners = new BlockVector3[]{cornerMinBlockVector3, cornerMaxBlockVector3};
            Vector spawnLocation = fromBlockVector3(spawnBlockVector3);
            Vector npcLocation = npcBlockVector3 == null ? null : fromBlockVector3(npcBlockVector3);
            Vector quarryLocation = quarryBlockVector3 == null ? null : fromBlockVector3(quarryBlockVector3);
            BoundingBox area = BoundingBox.of(fromBlockVector3(corners[0]), fromBlockVector3(corners[1]));

            MineBlocks mineBlocks = new MineBlocks(spawnLocation, area, npcLocation, quarryLocation);
            return mineBlocks;
        }

        private Vector fromBlockVector3(BlockVector3 blockVector3) {
            return new Vector(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
        }

        private Clipboard getClipboard(File schematicFile) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                return clipboard;
            } catch (IOException e) {
                PLUGIN_INSTANCE.logError("There was an error while trying to load the worldedit clipboard", e);
                return null;
            }
        }
    }
}
