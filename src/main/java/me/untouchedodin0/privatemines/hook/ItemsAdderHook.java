package me.untouchedodin0.privatemines.hook;

import dev.lone.itemsadder.api.CustomBlock;
import me.untouchedodin0.privatemines.mine.MineBlockHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

public class ItemsAdderHook extends Hook {
    public static final String ITEMSADDER_NAME = "ItemsAdder";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();
    private ItemsAdderBlockHandler blockHandler;

    @Override
    public String getPluginName() {
        return ITEMSADDER_NAME;
    }

    @Override
    public void hook() {
        String itemsAdderVersion = getPlugin().getPluginMeta().getVersion();
        PLUGIN_INSTANCE.logInfo(String.format("""
                Found ItemsAdder v%s installed,
                make sure to list the materials under the itemsadder section
                within each mine type if you want to use itemsadder
                blocks or it won't load correctly!""", itemsAdderVersion));
        blockHandler = new ItemsAdderBlockHandler();
    }


    public void removeItemsAdderBlocksFromBoundingBox(BoundingBox boundingBox) {
        for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                    Location location = new Location(MINES_WORLD, x, y, z);
                    blockHandler.remove(location);
                }
            }
        }
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
    }


}
