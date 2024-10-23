package me.untouchedodin0.privatemines.utils.schematic;

import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.iterator.SchematicIterator.MineBlocks;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import org.bukkit.Location;

public class PastedMine {

  private final PrivateMines privateMines = PrivateMines.getInstance();
  private final SchematicStorage schematicStorage = privateMines.getSchematicStorage();
  private File file;
  private final BlockVector3 pasteLocation;
  private BlockVector3 lowerRails;
  private BlockVector3 upperRails;
  private Location location;

  public PastedMine(BlockVector3 pasteLocation) {
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
    BlockVector3 spawnLocation = mineBlocks.getSpawnLocation();
    BlockVector3 corner1 = mineBlocks.getCorner1().add(0, 0, 1);
    BlockVector3 corner2 = mineBlocks.getCorner2().add(0, 0, 1);

    this.lowerRails = pasteLocation.subtract(spawnLocation).add(corner2);
    this.upperRails = pasteLocation.subtract(spawnLocation).add(corner1);

    return this;
  }

  public Location getLocation() {
    return location;
  }

  public Location getLowerRailsLocation() {
    return new Location(location.getWorld(), lowerRails.getBlockX(), lowerRails.getBlockY(), lowerRails.getBlockZ());
  }

  public Location getUpperRailsLocation() {
    return new Location(location.getWorld(), upperRails.getBlockX(), upperRails.getBlockY(), upperRails.getBlockZ());
  }
}