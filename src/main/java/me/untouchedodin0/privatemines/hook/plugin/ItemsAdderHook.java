package me.untouchedodin0.privatemines.hook.plugin;

import dev.lone.itemsadder.api.CustomBlock;
import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.MineBlockHandler;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineData;
import me.untouchedodin0.privatemines.mine.MineStructure;
import me.untouchedodin0.privatemines.mine.MineType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class ItemsAdderHook extends Hook {
    public static final String PLUGIN_NAME = "ItemsAdder";
    private static final World MINES_WORLD = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();
    private ItemsAdderBlockHandler blockHandler;

    @ConfigurationEntry(key = "should-create-wall-gap", section = "mine.configuration", value = "true", type = ConfigurationValueType.BOOLEAN)
    boolean shouldCreateWallGap;
    @ConfigurationEntry(key = "gap", section = "mine.configuration", value = "1", type = ConfigurationValueType.INT)
    int wallsGap;

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

    public void resetMineWithItemsAdder(Mine mine) {
        MineData mineData = mine.getMineData();
        MineType mineType = mineData.getMineType();
        MineStructure mineStructure = mineData.getMineStructure();

        PrivateMineResetEvent privateMineResetEvent = new PrivateMineResetEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineResetEvent);
        if (privateMineResetEvent.isCancelled()) return;

        BoundingBox boundingBox = mineStructure.mineBoundingBox();
        if (shouldCreateWallGap) boundingBox.expand(-Math.abs(wallsGap));
        HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO()
                .fill(boundingBox, 0, WorldEditHook.EMPTY);

        for (Player online : Bukkit.getOnlinePlayers()) {
            Location location = online.getLocation();
            boolean isPlayerInRegion = boundingBox.contains(location.toVector());
            if (isPlayerInRegion && location.getWorld().equals(MINES_WORLD)) mine.teleport(online);
        }

        for (int x = (int) Math.floor(boundingBox.getMinX()); x < Math.ceil(boundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(boundingBox.getMinX()); y < Math.ceil(boundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(boundingBox.getMinX()); z < Math.ceil(boundingBox.getMaxX()); z++) {
//                    String randomMaterial;
//                    oraxenBlockHandler.place(randomMaterial, new Location(MINES_WORLD, x, y, z));
                }
            }
        }
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
