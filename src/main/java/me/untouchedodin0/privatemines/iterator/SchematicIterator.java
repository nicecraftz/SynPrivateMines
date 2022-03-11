package me.untouchedodin0.privatemines.iterator;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import me.untouchedodin0.privatemines.storage.points.SchematicPoints;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematicIterator {

    SchematicStorage schematicStorage;
    BlockVector3 spawn;
    BlockVector3 corner1;
    BlockVector3 corner2;
    List<BlockVector3> corners = new ArrayList<>();
    List<BlockVector3> test = new ArrayList<>();
    BlockVector3 fix;
    BlockVector3 fix2 = null;

    public SchematicIterator(SchematicStorage storage) {
        this.schematicStorage = storage;
    }

    public MineBlocks findRelativePoints(File file) {
        MineBlocks mineBlocks = new MineBlocks();
        mineBlocks.corners = new BlockVector3[2];

        Clipboard clipboard;
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);

        if (clipboardFormat != null) {
            try (ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(file))) {
                clipboard = clipboardReader.read();
                Bukkit.getLogger().info("Clipboard: " + clipboard);
                Bukkit.getLogger().info("Clipboard Region: " + clipboard.getRegion());
//
                clipboard.getRegion().forEach(blockVector3 -> {
                    Material spawnMaterial = Material.SPONGE;
                    Material cornerMaterial = Material.POWERED_RAIL;

                    BlockType blockType = clipboard.getBlock(blockVector3).getBlockType();
                    if (blockType.equals(BlockTypes.POWERED_RAIL)) {
                        if (corner1 == null) {
                            Bukkit.getLogger().info("powered rail " + blockVector3.toParserString());
                            corner1 = BlockVector3.at(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
                        } else if (corner2 == null) {
                            Bukkit.getLogger().info("powered rail " + blockVector3.toParserString());
                            corner2 = BlockVector3.at(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
                        }
                    }
                });

                Bukkit.getLogger().info("corner1: " + corner1);
                Bukkit.getLogger().info("corner2: " + corner2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mineBlocks;
    }

    public static class MineBlocks {
        BlockVector3 spawnLocation;
        BlockVector3[] corners;

        public BlockVector3 getSpawnLocation() {
            return spawnLocation;
        }

        public BlockVector3[] getCorners() {
            return corners;
        }
    }
}
