package me.untouchedodin0.privatemines.iterator;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.hook.plugin.HookHandler;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.io.File;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;

public class SchematicIterator {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private BlockVector3 spawnBlockVector3;
    private BlockVector3 npcBlockVector3;
    private BlockVector3 quarryBlockVector3;
    private BlockVector3 cornerMinBlockVector3;
    private BlockVector3 cornerMaxBlockVector3;

    @ConfigurationEntry(key = "spawn-point", section = "materials", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material spawnMaterial;

    @ConfigurationEntry(key = "mine-corner", section = "materials", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material cornerMaterial;

    @ConfigurationEntry(key = "sell-npc", section = "materials", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material npcMaterial;

    @ConfigurationEntry(key = "quarry", section = "materials", type = ConfigurationValueType.MATERIAL, value = "POWERED_RAIL")
    private Material quarryMaterial;

    public SchematicIterator() {
        InstanceRegistry.registerInstance(this);
    }


    public MineBlocks findRelativePoints(File file) {
        try (
                Clipboard clipboard = HookHandler.get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                        .getWorldEditWorldWriter()
                        .getClipboard(file)
        ) {
            Region region = clipboard.getRegion();
            for (BlockVector3 regionBlock : region) {
                BlockType blockType = clipboard.getBlock(regionBlock).getBlockType();
                BlockVector3 baseVector = BlockVector3.at(regionBlock.getX(), regionBlock.getY(), regionBlock.getZ());
                Material worldEditToBukkitMaterial = adapt(blockType);

                if (worldEditToBukkitMaterial == cornerMaterial) {
                    if (cornerMinBlockVector3 == null) {
                        cornerMinBlockVector3 = baseVector;
                    } else if (cornerMaxBlockVector3 == null) {
                        cornerMaxBlockVector3 = baseVector;
                    }
                } else if (worldEditToBukkitMaterial == spawnMaterial && spawnBlockVector3 == null) {
                    spawnBlockVector3 = baseVector;
                } else if (worldEditToBukkitMaterial == npcMaterial && npcBlockVector3 == null) {
                    npcBlockVector3 = baseVector;
                } else if (worldEditToBukkitMaterial == quarryMaterial && quarryBlockVector3 == null) {
                    quarryBlockVector3 = baseVector;
                }
            }
        }

        if (spawnBlockVector3 == null || cornerMinBlockVector3 == null || cornerMaxBlockVector3 == null) {
            PRIVATE_MINES.logInfo("Please, make sure you have placed the spawn and corner blocks.");
            return null;
        }

        BlockVector3[] corners = new BlockVector3[]{cornerMinBlockVector3, cornerMaxBlockVector3};
        MineBlocks mineBlocks = MineBlocks.fromWorldGuardElements(spawnBlockVector3,
                corners,
                npcBlockVector3,
                quarryBlockVector3
        );

        return mineBlocks;
    }

    public record MineBlocks(
            Vector spawnLocation, Vector[] corners, Vector npcLocation, Vector quarryLocation
    ) {
        public static MineBlocks fromWorldGuardElements(BlockVector3 spawnLocation, BlockVector3[] corners, BlockVector3 npcLocation, BlockVector3 quarryLocation) {
            Vector bukkitSpawnLocation = adapt(spawnLocation);
            Vector bukkitNpcLocation = npcLocation == null ? null : adapt(npcLocation);
            Vector bukkitQuarryLocation = quarryLocation == null ? null : adapt(quarryLocation);
            Vector bukkitFirstCorner = adapt(corners[0]);
            Vector bukkitSecondCorner = adapt(corners[1]);
            Vector[] locations = new Vector[]{bukkitFirstCorner, bukkitSecondCorner};
            return new MineBlocks(bukkitSpawnLocation, locations, bukkitNpcLocation, bukkitQuarryLocation);
        }

        private static Vector adapt(BlockVector3 blockVector3) {
            return new Vector(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
        }

    }

}
