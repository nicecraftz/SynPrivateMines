package me.untouchedodin0.privatemines.mine;

import com.google.common.collect.Maps;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.events.PrivateMineDeleteEvent;
import me.untouchedodin0.privatemines.events.PrivateMineUpgradeEvent;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.utils.hook.HookHandler;
import me.untouchedodin0.privatemines.utils.hook.ItemsAdderHook;
import me.untouchedodin0.privatemines.utils.schematic.PasteHelper;
import me.untouchedodin0.privatemines.utils.schematic.PastedMine;
import me.untouchedodin0.privatemines.utils.schematic.WorldEditWorldWriter;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import redempt.redlib.misc.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MineService {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private static final MineWorldManager MINE_WORLD_MANAGER = PRIVATE_MINES.getMineWorldManager();
    private static World MINES_WORLD = MINE_WORLD_MANAGER.getMinesWorld();
    private final HashMap<UUID, Mine> mines = Maps.newHashMap();
//    private final DatabaseSomething impl;

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

    private void handleMineDeletion(Mine mine) {
        MineData mineData = mine.getMineData();
        UUID uuid = mineData.getMineOwner();

        PrivateMineDeleteEvent privateMineDeleteEvent = new PrivateMineDeleteEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineDeleteEvent);
        if (privateMineDeleteEvent.isCancelled()) return;

        MineStructure structure = mineData.getMineStructure();
        BoundingBox schematicBoundingBox = structure.schematicBoundingBox();
        BoundingBox mineBoundingBox = structure.mineBoundingBox();

        // todo: this could be just made a constant to accept a uuid parameter or just the mine object.
        String regionName = String.format("mine-%s", uuid);
        String fullRegionName = String.format("full-mine-%s", uuid);

        PrivateMines.getInstance().getRegionOrchestrator().removeRegion(regionName);
        PrivateMines.getInstance().getRegionOrchestrator().removeRegion(fullRegionName);
        WorldEditWorldWriter.getWriter().fill(MINES_WORLD, schematicBoundingBox, List.of(Material.AIR));

        if (mineData.getMineType().useItemsAdder() && HookHandler.isHooked(ItemsAdderHook.ITEMSADDER_NAME)) {
            ItemsAdderHook.removeItemsAdderBlocksFromBoundingBox(MINES_WORLD, mineBoundingBox);
        }

        mine.stopTasks();

//        todo: make this better
//        databaseSomething.delete(mine);
        SQLUtils.delete(mine);
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

        PrivateMineUpgradeEvent privateMineUpgradeEvent = new PrivateMineUpgradeEvent(
                mine,
                currentType,
                nextType
        );

        Bukkit.getPluginManager().callEvent(privateMineUpgradeEvent);
        if (privateMineUpgradeEvent.isCancelled()) {
            return;
        }

        if (currentType.schematicFile().equals(nextType.schematicFile())) return;
        PRIVATE_MINES.getEconomy().withdrawPlayer(player, nextType.upgradeCost());
        String mineRegionName = String.format("mine-%s", player.getUniqueId());
        String fullRegionName = String.format("full-mine-%s", player.getUniqueId());

        BoundingBox schematicArea = mineData.getMineStructure().schematicBoundingBox();


        Task.asyncDelayed(() -> {
            WorldEditWorldWriter.getWriter()
                    .fill(MINE_WORLD_MANAGER.getMinesWorld(), schematicArea, List.of(Material.AIR));
            PasteHelper pasteHelper = PrivateMines.getInstance().getPasteHelper();
            PastedMine pastedMine = pasteHelper.paste(nextType.schematicFile(), mine.getMineLocation());

            Location spawn = mine.getMineLocation().clone().add(0, 0, 1);
            Location corner1 = pastedMine.getLowerRailsLocation();
            Location corner2 = pastedMine.getUpperRailsLocation();

            Location minimum = pasteHelper.getMinimum();
            Location maximum = pasteHelper.getMaximum();

            BoundingBox mineRegion = BoundingBox.of(corner1, corner2);
            BoundingBox schematicRegion = BoundingBox.of(minimum, maximum);

            PrivateMines.getInstance().getRegionOrchestrator().addRegion(mineRegionName, mineRegion);
            PrivateMines.getInstance().getRegionOrchestrator().addRegion(fullRegionName, schematicRegion);

            MineStructure mineStructure = MineStructure.fromBoundingBoxes(
                    MINES_WORLD,
                    mineRegion,
                    schematicRegion,
                    mineData.getMineStructure()
                            .mineLocation(),
                    spawn
            );

            mineData.setMineStructure(mineStructure);
            mine.handleReset();
            Task.asyncDelayed(() -> SQLUtils.update(mine));
            Task.syncDelayed(() -> spawn.getBlock().setType(Material.AIR, false));

        });
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
