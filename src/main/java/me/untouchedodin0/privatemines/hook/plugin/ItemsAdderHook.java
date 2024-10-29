package me.untouchedodin0.privatemines.hook.plugin;

import dev.lone.itemsadder.api.CustomBlock;
import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.Entry;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.MineBlockHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.Map;

public class ItemsAdderHook extends Hook {
    public static final String PLUGIN_NAME = "ItemsAdder";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineService().getMinesWorld();
    private ItemsAdderBlockHandler blockHandler;

    public ItemsAdderHook() {
        ConfigurationInstanceRegistry.registerInstance(this);
    }


    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void hook() {
        String itemsAdderVersion = getPlugin().getPluginMeta().getVersion();
        LoggerUtil.info("""
                Found ItemsAdder v%s installed,
                make sure to list the materials under the itemsadder section
                within each mine type if you want to use itemsadder
                blocks or it won't load correctly!""", itemsAdderVersion);
        blockHandler = new ItemsAdderBlockHandler();
    }

    public ItemsAdderBlockHandler getBlockHandler() {
        return blockHandler;
    }

    public static class ItemsAdderBlockHandler implements MineBlockHandler {
        @Override
        public String getPrefix() {
            return "itemsadder";
        }

        @Override
        public void place(String id, Location location) {
            Block block = location.getBlock();
            if (!CustomBlock.isInRegistry(id)) return;
            CustomBlock customBlock = CustomBlock.getInstance(id);
            customBlock.place(block.getLocation());
        }

        @Override
        public void remove(Location location) {
            Block block = location.getBlock();
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
            if (customBlock != null) customBlock.remove();
        }

        @Override
        public void removeFromArea(BoundingBox boundingBox) {
            for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
                for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                    for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                        Location location = new Location(MINES_WORLD, x, y, z);
                        remove(location);
                    }
                }
            }
        }

        @Override
        public void placeInAreaWithChance(Map<String, Double> blocks, BoundingBox boundingBox) {

        }
    }


}
