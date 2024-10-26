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
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.WorldWriter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class WorldEditHook extends Hook {
    public static final Map<Material, Double> EMPTY = Map.of(Material.AIR, 1d);
    public static final String PLUGIN_NAME = "WorldEdit";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();
    private WorldEdit worldEdit;
    private WorldEditWorldWriter worldEditWorldWriter;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void hook() {
        worldEdit = WorldEdit.getInstance();
        worldEditWorldWriter = new WorldEditWorldWriter();
    }

    public WorldEditWorldWriter getWorldEditWorldWriter() {
        return worldEditWorldWriter;
    }

    public class WorldEditWorldWriter implements WorldWriter<Clipboard> {

        @Override
        public void placeSchematic(File schematicFile, Location location) {
            BlockVector3 adaptedLocation = BukkitAdapter.asBlockVector(location);
            com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(location.getWorld());

            try (
                    Clipboard clipboard = getClipboard(schematicFile); EditSession editSession = worldEdit.newEditSession(
                    worldEditWorld)
            ) {
                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(adaptedLocation)
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);
            }
        }

        @Override
        public Clipboard getClipboard(File schematicFile) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                return clipboard;
            } catch (IOException e) {
                PLUGIN_INSTANCE.logError("There was an error while trying to load the worldedit clipboard", e);
                return null;
            }
        }

        @Override
        public void fill(BoundingBox boundingBox, Map<Material, Double> materials) {
            RandomPattern randomPattern = new RandomPattern();

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
        public void fillWithGap(BoundingBox boundingBox, int gap, Map<Material, Double> materials) {
            fill(boundingBox, Map.of(Material.AIR, 1d));
            boundingBox.expand(-Math.abs(gap));
            fill(boundingBox, materials);
        }
    }
}
