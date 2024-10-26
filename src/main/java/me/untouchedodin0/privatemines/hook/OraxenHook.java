package me.untouchedodin0.privatemines.hook;

import io.th0rgal.oraxen.api.OraxenBlocks;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.hook.WorldEditWorldWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class OraxenHook extends Hook {
    public static final String ORAXEN_NAME = "Oraxen";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();
    private OraxenBlockHandler oraxenBlockHandler;

    @ConfigurationEntry(key = "should-create-wall-gap", section = "mine.configuration", value = "true", type = ConfigurationValueType.BOOLEAN)
    boolean shouldCreateWallGap;
    @ConfigurationEntry(key = "gap", section = "mine.configuration", value = "1", type = ConfigurationValueType.INT)
    int wallsGap;

    public OraxenHook() {
        InstanceRegistry.registerInstance(this);
    }

    @Override
    public String getPluginName() {
        return ORAXEN_NAME;
    }

    @Override
    public void hook() {
        String oraxenVersion = getPlugin().getPluginMeta().getVersion();
        PLUGIN_INSTANCE.logInfo(String.format("""
                Found Oraxen v%s installed,
                make sure to list the materials under the oraxen section
                within each mine type if you want to use oraxen
                blocks or it won't load correctly!""", oraxenVersion));
        oraxenBlockHandler = new OraxenBlockHandler();
    }

    public void resetMineWithOraxen(Mine mine) {
        MineData mineData = mine.getMineData();
        MineType mineType = mineData.getMineType();
        MineStructure mineStructure = mineData.getMineStructure();

        PrivateMineResetEvent privateMineResetEvent = new PrivateMineResetEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineResetEvent);
        if (privateMineResetEvent.isCancelled()) return;

        BoundingBox boundingBox = mineStructure.mineBoundingBox();
        if (shouldCreateWallGap) boundingBox.expand(-Math.abs(wallsGap));
        WorldEditWorldWriter.getWriter().fill(boundingBox, mineType.materials());

        for (Player online : Bukkit.getOnlinePlayers()) {
            Location location = online.getLocation();
            boolean isPlayerInRegion = boundingBox.contains(location.toVector());
            if (isPlayerInRegion && location.getWorld().equals(MINES_WORLD)) mine.teleport(player);
        }

        for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                    String randomMaterial;
                    oraxenBlockHandler.place();
                    String randomBlock = mine.getMineData().getMaterials();
                    Location location = new Location(MINES_WORLD, x, y, z);
                    location.getBlock().setType(Material.AIR, false);
                    OraxenBlocks.place(randomBlock, location);
                }
            }
        }

        int i = (int) Math.min(min.getX(), max.getX());
        int j = (int) Math.min(min.getY(), max.getY());
        int k = (int) Math.min(min.getZ(), max.getZ());
        int m = (int) Math.max(min.getX(), max.getX());
        int n = (int) Math.max(min.getY(), max.getY());
        int i1 = (int) Math.max(min.getZ(), max.getZ());

        for (int i2 = i; i2 <= m; i2++) {
            for (int i3 = j; i3 <= n; i3++) {
                for (int i4 = k; i4 <= i1; i4++) {
                    String random = weightedRandom.roll();
                    Block block = world.getBlockAt(i2, i3, i4);
                    Task.syncDelayed(() -> block.setType(Material.AIR, false));
                    Task.syncDelayed(() -> OraxenBlocks.place(random, block.getLocation()));
                }
            }
        }
    }

    public static class OraxenBlockHandler implements MineBlockHandler {

        @Override
        public String getPrefix() {
            return "oraxen";
        }

        @Override
        public void place(String id, Location location) {
            location.getBlock().setType(Material.AIR, false);
            if (!OraxenBlocks.isOraxenBlock(id)) return;
            OraxenBlocks.place(id, location);
        }

        @Override
        public void remove(Location location) {
            OraxenBlocks.remove(location, null);
        }
    }
}
