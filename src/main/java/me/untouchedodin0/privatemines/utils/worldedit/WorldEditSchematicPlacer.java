package me.untouchedodin0.privatemines.utils.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.untouchedodin0.privatemines.PrivateMines;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class WorldEditSchematicPlacer implements SchematicPlacer<Clipboard> {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private static final WorldEdit WORLD_EDIT = WorldEdit.getInstance();
    private static WorldEditSchematicPlacer placer;

    private WorldEditSchematicPlacer() {

    }


    @Override
    public void placeSchematic(File schematicFile, Location location) {
        BlockVector3 adaptedLocation = BukkitAdapter.asBlockVector(location);
        World worldEditWorld = BukkitAdapter.adapt(location.getWorld());

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

    public static WorldEditSchematicPlacer getPlacer() {
        if (placer == null) placer = new WorldEditSchematicPlacer();
        return placer;
    }

}
