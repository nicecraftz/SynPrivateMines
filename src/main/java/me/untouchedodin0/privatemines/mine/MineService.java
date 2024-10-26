package me.untouchedodin0.privatemines.mine;

import com.google.common.collect.Maps;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.events.PrivateMineDeleteEvent;
import me.untouchedodin0.privatemines.events.PrivateMineExpandEvent;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.events.PrivateMineUpgradeEvent;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.hook.plugin.*;
import me.untouchedodin0.privatemines.utils.schematic.PasteHelper;
import me.untouchedodin0.privatemines.utils.schematic.PastedMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    @ConfigurationEntry(key = "border-upgrade", section = "mine", value = "true", type = ConfigurationValueType.BOOLEAN)
    boolean borderUpgrade;

    @ConfigurationEntry(key = "upgrade", section = "materials", value = "OBSIDIAN", type = ConfigurationValueType.MATERIAL)
    Material upgradeMaterial;

    @ConfigurationEntry(key = "gap", section = "mine.configuration", value = "1", type = ConfigurationValueType.INT)
    int wallsGap;

    @ConfigurationEntry(key = "walls-go-up", section = "mine.configuration", value = "false", type = ConfigurationValueType.BOOLEAN)
    boolean wallsGoUp;

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

    public void create(UUID uuid, Location location, MineType mineType) {
        Mine mine = PRIVATE_MINES.getMineFactory().create(uuid, location, mineType);
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

        WorldEditHook.WorldEditWorldWriter worldEditWorldWriter = HookHandler.get(
                WorldEditHook.PLUGIN_NAME,
                WorldEditHook.class
        ).getWorldEditWorldWriter();

        if (shouldCreateWallGap) {
            worldEditWorldWriter.fillWithGap(boundingBox, wallsGap, materials);
        } else {
            worldEditWorldWriter.fill(boundingBox, materials);
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

        HookHandler.get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldWriter()
                .fill(schematicBoundingBox, WorldEditHook.EMPTY);

        if (mineData.getMineType().useItemsAdder() && HookHandler.hooked(ItemsAdderHook.PLUGIN_NAME)) {
            HookHandler.get(ItemsAdderHook.PLUGIN_NAME, ItemsAdderHook.class)
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


        HookHandler.get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldWriter()
                .fill(schematicArea, WorldEditHook.EMPTY);

        PasteHelper pasteHelper = PrivateMines.getInstance().getPasteHelper();
        PastedMine pastedMine = pasteHelper.paste(nextType.schematicFile(), mine.getMineLocation());
        Location spawn = mine.getMineLocation().clone().add(0, 0, 1);

        Location mineMinCorner = pastedMine.getLowerRailsLocation();
        Location mineMaxCorner = pastedMine.getUpperRailsLocation();
        Location schematicMinCorner = pasteHelper.getMinimum();
        Location schematicMaxCorner = pasteHelper.getMaximum();

        BoundingBox mineRegion = BoundingBox.of(mineMinCorner, mineMaxCorner);
        BoundingBox schematicRegion = BoundingBox.of(schematicMinCorner, schematicMaxCorner);

        RegionOrchestrator<ProtectedRegion, Flag> regionOrchestrator = HookHandler.get(
                WorldGuardHook.PLUGIN_NAME,
                WorldGuardHook.class
        ).getRegionOrchestrator();

        regionOrchestrator.addRegion(mineRegionName, mineRegion);
        regionOrchestrator.addRegion(fullRegionName, schematicRegion);

        MineStructure mineStructure = new MineStructure(
                mineRegion,
                schematicRegion,
                mineData.getMineStructure().mineLocation(),
                spawn
        );


        mineData.setMineStructure(mineStructure);
        handleReset(mine);

//        Task.asyncDelayed(() -> SQLUtils.update(mine));

        spawn.getBlock().setType(Material.AIR);
    }

    public void handleReset(Mine mine) {
        MineData mineData = mine.getMineData();
        MineType mineType = mineData.getMineType();

        boolean useAndResetWithOraxen = mineType.useOraxen() && HookHandler.hooked(OraxenHook.PLUGIN_NAME);
        boolean useAndResetWithItemsAdder = mineType.useItemsAdder() && HookHandler.hooked(ItemsAdderHook.PLUGIN_NAME);

        if (!useAndResetWithItemsAdder || !useAndResetWithOraxen) {
            reset(mine);
        } else if (useAndResetWithItemsAdder) {
            HookHandler.get(ItemsAdderHook.PLUGIN_NAME, ItemsAdderHook.class).resetMineWithItemsAdder(mine);
        } else if (useAndResetWithOraxen) {
            HookHandler.get(OraxenHook.PLUGIN_NAME, OraxenHook.class).resetMineWithOraxen(mine);
        }

        World minesWorld = PrivateMines.getInstance().getMineWorldManager().getMinesWorld();

        BoundingBox mineBoundingBox = mineData.getMineStructure().mineBoundingBox();
        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean isPlayerInRegion = mineBoundingBox.contains(online.getLocation().toVector());
            boolean inWorld = online.getWorld().equals(minesWorld);
            if (isPlayerInRegion && inWorld) mine.teleport(online);
        }
    }


    public void expand(Mine mine, int amount) {
        if (!canExpand(mine, amount)) return;

        MineStructure mineStructure = mine.getMineData().getMineStructure();

        BoundingBox airFillRegion = mineStructure.mineBoundingBox().clone();
        BoundingBox wallsRegion = mineStructure.mineBoundingBox().clone();
        BoundingBox mineBoundingBox = mineStructure.mineBoundingBox().clone();

        mineBoundingBox.expand(1);
        wallsRegion.expand(1);

        if (wallsGoUp) {
            wallsRegion.expand(-1);
        } else {
            wallsRegion.expand(-1, 0, -1);
        }

        airFillRegion.expand(1, 1, 1);

        Map<Material, Double> materials = mine.getMineData().getMineType().materials();
        PrivateMineExpandEvent privateMineExpandEvent = new PrivateMineExpandEvent(mine, mineBoundingBox);
        Bukkit.getPluginManager().callEvent(privateMineExpandEvent);
        if (privateMineExpandEvent.isCancelled()) return;

        WorldEditHook.WorldEditWorldWriter worldEditWorldWriter = HookHandler.get(
                WorldEditHook.PLUGIN_NAME,
                WorldEditHook.class
        ).getWorldEditWorldWriter();

        worldEditWorldWriter.fill(wallsRegion, Map.of(Material.BEDROCK, 1d));
        worldEditWorldWriter.fill(airFillRegion, Map.of(Material.AIR, 1d));

        BoundingBox schematicBoundingBox = mineStructure.schematicBoundingBox().clone();
        schematicBoundingBox.expand(1, 1, 1);

        MineStructure newMineStructure = new MineStructure(
                mineBoundingBox,
                schematicBoundingBox,
                mineStructure.mineLocation(),
                mineStructure.spawnLocation()
        );

        String mineRegionName = String.format("mineRegion-%s", mine.getMineData().getMineOwner());

        HookHandler.get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator()
                .addRegion(mineRegionName, mineBoundingBox);

        mine.getMineData().setMineStructure(newMineStructure);
        handleReset(mine);
//        SQLUtils.update(this);
    }

    public boolean canExpand(Mine mine, int amount) {
        MineStructure structure = mine.getMineData().getMineStructure();
        BoundingBox mineBoundingBox = structure.mineBoundingBox();
        mineBoundingBox.expand(amount);

        boolean canExpand = true;
        for (int x = (int) Math.floor(mineBoundingBox.getMinX()); x < Math.ceil(mineBoundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(mineBoundingBox.getMinX()); y < Math.ceil(mineBoundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(mineBoundingBox.getMinX()); z < Math.ceil(mineBoundingBox.getMaxX()); z++) {
                    Location location = new Location(PRIVATE_MINES.getMineWorldManager().getMinesWorld(), x, y, z);
                    Material material = location.getBlock().getType();
                    if (material != upgradeMaterial) continue;
                    canExpand = false;
                    if (borderUpgrade) upgrade(mine);
                }
            }
        }

        return canExpand;
    }

    public boolean has(UUID uuid) {
        return mines.containsKey(uuid);
    }

    public Mine get(UUID uuid) {
        return mines.get(uuid);
    }

    public Mine getNearest(Location location) {
        if (!PRIVATE_MINES.getMineWorldManager().getMinesWorld().equals(location.getWorld())) {
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
