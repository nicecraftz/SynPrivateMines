package me.untouchedodin0.privatemines.utils.hook;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import redempt.redlib.blockdata.custom.CustomBlock;

public class ItemsAdderHook extends Hook {
    public static final String ITEMSADDER_NAME = "ItemsAdder";

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

    }


    public static void removeItemsAdderBlocksFromBoundingBox(World world, BoundingBox boundingBox) {
        for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
                    Location location = new Location(world, x, y, z);
                    Block block = location.getBlock();
                    CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
                    if (customBlock != null) customBlock.remove();
                }
            }
        }
    }


}
