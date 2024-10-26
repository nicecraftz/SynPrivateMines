package me.untouchedodin0.privatemines.utils.schematic;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.iterator.SchematicIterator.MineBlocks;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;

public class PastedMine {

    private final PrivateMines privateMines = PrivateMines.getInstance();
    private final SchematicStorage schematicStorage = privateMines.getSchematicStorage();
    private File file;

    private Vector pasteLocation;
    private Vector lowerRails;
    private Vector upperRails;

    private Location location;

    public PastedMine(Vector pasteLocation) {
        this.pasteLocation = pasteLocation;
    }

    public PastedMine setLocation(Location location) {
        this.location = location;
        return this;
    }

    public PastedMine setFile(File file) {
        this.file = file;
        return this;
    }

    public PastedMine create() {
        MineBlocks mineBlocks = schematicStorage.get(file);
        Vector spawnLocation = mineBlocks.spawnLocation();
        Vector firstCorner = mineBlocks.corners()[0].add(new Vector(0, 0, 1));
        Vector secondCorner = mineBlocks.corners()[1].add(new Vector(0, 0, 1));

        this.lowerRails = pasteLocation.subtract(spawnLocation).add(secondCorner);
        this.upperRails = pasteLocation.subtract(spawnLocation).add(firstCorner);
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public Location getLowerRailsLocation() {
        return new Location(
                location.getWorld(),
                lowerRails.getBlockX(),
                lowerRails.getBlockY(),
                lowerRails.getBlockZ()
        );
    }

    public Location getUpperRailsLocation() {
        return new Location(
                location.getWorld(),
                upperRails.getBlockX(),
                upperRails.getBlockY(),
                upperRails.getBlockZ()
        );
    }
}