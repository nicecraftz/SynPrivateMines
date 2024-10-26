package me.untouchedodin0.privatemines.mine;

import com.google.common.collect.Maps;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineDeleteEvent;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.events.PrivateMineUpgradeEvent;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.ItemsAdderHook;
import me.untouchedodin0.privatemines.hook.WorldGuardHook;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.utils.schematic.PasteHelper;
import me.untouchedodin0.privatemines.utils.schematic.PastedMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineService {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private final HashMap<UUID, Mine> mines = Maps.newHashMap();

    @ConfigurationEntry(key = "should-create-wall-gap", section = "mine.configuration", value = "true", type = ConfigurationValueType.BOOLEAN)
    boolean shouldCreateWallGap;

    @ConfigurationEntry(key = "gap", section = "mine.configuration", value = "1", type = ConfigurationValueType.INT)
    int wallsGap;

    public MineService() {
        InstanceRegistry.registerInstance(this);
    }

    public void cache(Mine mine) {
        mines.put(mine.getMineData().getMineOwner(), mine);
    }

    public boolean uncache(Mine mine) {
        return uncache(mine.getMineData().getMineOwner());
    }

    public boolean uncache(UUID uuid) {
        Mine mine = mines.remove(uuid);
        return mine != null;
    }

    public void create(Mine mine) {
        cache(mine);
        // todo handle mine creation
    }

    public void delete(Mine mine) {
        uncache(mine);
        handleMineDeletion(mine);
        // todo handle mine deletion
    }

    public void reset(Mine mine) {
        MineData mineData = mine.getMineData();
        MineType mineType = mineData.getMineType();
        BoundingBox boundingBox = mineData.getMineStructure().mineBoundingBox();

        // todo: handle personal blocks too
        Map<Material, Double> materials = mineType.materials();

        // todo: handle better
        // WeightedCollection<String> personalBlocks = mineType.personalBlocks();

        PrivateMineResetEvent privateMineResetEvent = new PrivateMineResetEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineResetEvent);
        if (privateMineResetEvent.isCancelled()) return;
        if (materials != null && materials.isEmpty()) return;

        if (shouldCreateWallGap) {
            WorldEditWorldWriter.getWriter().fillWithGap(boundingBox, wallsGap, materials);
        } else {
            WorldEditWorldWriter.getWriter().fill(boundingBox, materials);
        }
    }

    private void handleMineDeletion(Mine mine) {
        MineData mineData = mine.getMineData();
        PrivateMineDeleteEvent privateMineDeleteEvent = new PrivateMineDeleteEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineDeleteEvent);
        if (privateMineDeleteEvent.isCancelled()) return;

        MineStructure structure = mineData.getMineStructure();
        BoundingBox schematicBoundingBox = structure.schematicBoundingBox();
        BoundingBox mineBoundingBox = structure.mineBoundingBox();

        HookHandler.get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator()
                .removeMineRegions(mine);

        WorldEditWorldWriter.getWriter().fill(schematicBoundingBox, WorldEditWorldWriter.EMPTY);

        if (mineData.getMineType().useItemsAdder() && HookHandler.hooked(ItemsAdderHook.ITEMSADDER_NAME)) {
            HookHandler.get(ItemsAdderHook.ITEMSADDER_NAME, ItemsAdderHook.class)
                    .removeItemsAdderBlocksFromBoundingBox(mineBoundingBox);
        }

        mine.stopTasks();

//        todo: make this better
//        databaseSomething.delete(mine);
//        SQLUtils.delete(mine);
    }

    public void upgrade(Mine mine) {
        MineTypeRegistry mineTypeRegistry = PrivateMines.getInstance().getMineTypeRegistry();
        MineData mineData = mine.getMineData();
        UUID mineOwner = mineData.getMineOwner();

        Player player = Bukkit.getOfflinePlayer(mineOwner).getPlayer();
        if (player == null || !player.isOnline()) return;

        MineType currentType = mineData.getMineType();
        MineType nextType = mineTypeRegistry.getNextMineType(mineData.getMineType());
        mineData.setMineType(nextType);

        PrivateMineUpgradeEvent privateMineUpgradeEvent = new PrivateMineUpgradeEvent(mine, currentType, nextType);

        Bukkit.getPluginManager().callEvent(privateMineUpgradeEvent);
        if (privateMineUpgradeEvent.isCancelled()) return;

        if (currentType.schematicFile().equals(nextType.schematicFile())) return;
        PRIVATE_MINES.getEconomy().withdrawPlayer(player, nextType.upgradeCost());

        String mineRegionName = String.format("mine-%s", player.getUniqueId());
        String fullRegionName = String.format("full-mine-%s", player.getUniqueId());
        BoundingBox schematicArea = mineData.getMineStructure().schematicBoundingBox();

        WorldEditWorldWriter.getWriter().fill(schematicArea, WorldEditWorldWriter.EMPTY);

        PasteHelper pasteHelper = PrivateMines.getInstance().getPasteHelper();
        PastedMine pastedMine = pasteHelper.paste(nextType.schematicFile(), mine.getMineLocation());
        Location spawn = mine.getMineLocation().clone().add(0, 0, 1);

        Location mineMinCorner = pastedMine.getLowerRailsLocation();
        Location mineMaxCorner = pastedMine.getUpperRailsLocation();
        Location schematicMinCorner = pasteHelper.getMinimum();
        Location schematicMaxCorner = pasteHelper.getMaximum();

        BoundingBox mineRegion = BoundingBox.of(mineMinCorner, mineMaxCorner);
        BoundingBox schematicRegion = BoundingBox.of(schematicMinCorner, schematicMaxCorner);

        PrivateMines.getInstance().getRegionOrchestrator().addRegion(mineRegionName, mineRegion);
        PrivateMines.getInstance().getRegionOrchestrator().addRegion(fullRegionName, schematicRegion);

        MineStructure mineStructure = MineStructure.fromBoundingBoxes(
                MINES_WORLD,
                mineRegion,
                schematicRegion,
                mineData.getMineStructure().mineLocation(),
                spawn
        );

        mineData.setMineStructure(mineStructure);
        mine.handleReset();

        Task.asyncDelayed(() -> SQLUtils.update(mine));

        spawn.getBlock().setType(Material.AIR);
    }

    public boolean has(UUID uuid) {
        return mines.containsKey(uuid);
    }

    public Mine get(UUID uuid) {
        return mines.get(uuid);
    }

    public Mine getNearest(Location location) {
        if (!MINE_WORLD_MANAGER.getMinesWorld().equals(location.getWorld())) {
            throw new RuntimeException("Invalid world fetching");
        }

        Mine nearest = null;
        for (Mine mine : mines.values()) {
            if (nearest == null || distanceToMine(location, mine) < distanceToMine(location, nearest)) {
                nearest = mine;
            }
        }

        return nearest;
    }

    private double distanceToMine(Location location, Mine mine) {
        return location.distance(mine.getMineLocation());
    }

    public Map<UUID, Mine> getMines() {
        return Map.copyOf(mines);
    }

    public int getMinesCount() {
        return mines.size();
    }

}
