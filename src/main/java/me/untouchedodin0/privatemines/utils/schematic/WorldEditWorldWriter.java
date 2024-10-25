package me.untouchedodin0.privatemines.utils.schematic;

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
import me.untouchedodin0.privatemines.PrivateMines;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class WorldEditWorldWriter implements WorldWriter<Clipboard> {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private static final WorldEdit WORLD_EDIT = WorldEdit.getInstance();
    private static WorldEditWorldWriter placer;

    private WorldEditWorldWriter() {

    }


    @Override
    public void placeSchematic(File schematicFile, Location location) {
        BlockVector3 adaptedLocation = BukkitAdapter.asBlockVector(location);
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(location.getWorld());

        try (
                Clipboard clipboard = getClipboard(schematicFile); EditSession editSession = WORLD_EDIT.newEditSession(
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
            PRIVATE_MINES.logError("There was an error while trying to load the worldedit clipboard", e);
            return null;
        }
    }

    @Override
    public void fill(World world, BoundingBox boundingBox, List<Material> materialList) {
        RandomPattern randomPattern = new RandomPattern();
        for (Material material : materialList) {
            randomPattern.add(BukkitAdapter.adapt(material.createBlockData()), 1);
        }

        try (
                EditSession editSession = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(BukkitAdapter.adapt(world))
                        .fastMode(true)
                        .build()
        ) {
            BlockVector3 min = BukkitAdapter.asBlockVector(boundingBox.getMin().toLocation(world));
            BlockVector3 max = BukkitAdapter.asBlockVector(boundingBox.getMax().toLocation(world));
            Region region = new CuboidRegion(BukkitAdapter.adapt(world), min, max);

            editSession.setBlocks(region, randomPattern);
        }
    }

    public static WorldEditWorldWriter getWriter() {
        if (placer == null) placer = new WorldEditWorldWriter();
        return placer;
    }

}
