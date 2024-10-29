package me.untouchedodin0.privatemines.hook.plugin;

import io.th0rgal.oraxen.api.OraxenBlocks;
import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.MineBlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.Map;

public class OraxenHook extends Hook {
    public static final String PLUGIN_NAME = "Oraxen";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineService().getMinesWorld();
    private OraxenBlockHandler oraxenBlockHandler;

    public OraxenHook() {
        ConfigurationInstanceRegistry.registerInstance(this);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void hook() {
        String oraxenVersion = getPlugin().getPluginMeta().getVersion();
        LoggerUtil.info("""
                Found Oraxen v%s installed,
                make sure to list the materials under the oraxen section
                within each mine type if you want to use oraxen
                blocks or it won't load correctly!""", oraxenVersion);
        oraxenBlockHandler = new OraxenBlockHandler();
    }

    public OraxenBlockHandler getOraxenBlockHandler() {
        return oraxenBlockHandler;
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

        @Override
        public void removeFromArea(BoundingBox boundingBox) {
            for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
                for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                    for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                        remove(new Location(MINES_WORLD, x, y, z));
                    }
                }
            }
        }

        @Override
        public void placeInAreaWithChance(Map<String, Double> blocks, BoundingBox boundingBox) {

        }
    }
}
