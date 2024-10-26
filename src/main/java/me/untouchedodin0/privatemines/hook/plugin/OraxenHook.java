package me.untouchedodin0.privatemines.hook.plugin;

import io.th0rgal.oraxen.api.OraxenBlocks;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.MineBlockHandler;
import me.untouchedodin0.privatemines.mine.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class OraxenHook extends Hook {
    public static final String PLUGIN_NAME = "Oraxen";
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
        return PLUGIN_NAME;
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
        HookHandler.get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldWriter().fill(boundingBox, WorldEditHook.EMPTY);

        for (Player online : Bukkit.getOnlinePlayers()) {
            Location location = online.getLocation();
            boolean isPlayerInRegion = boundingBox.contains(location.toVector());
            if (isPlayerInRegion && location.getWorld().equals(MINES_WORLD)) mine.teleport(online);
        }

//        for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
//            for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
//                for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
//                    String randomMaterial;
//                    oraxenBlockHandler.place(randomMaterial, new Location(MINES_WORLD, x, y, z));
//                }
//            }
//        }
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
