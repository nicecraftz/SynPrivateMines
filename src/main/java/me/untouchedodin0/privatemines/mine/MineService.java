package me.untouchedodin0.privatemines.mine;

import com.google.common.collect.Maps;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.events.PrivateMineDeleteEvent;
import me.untouchedodin0.privatemines.events.PrivateMineExpandEvent;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.events.PrivateMineUpgradeEvent;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.hook.WorldIO;
import me.untouchedodin0.privatemines.hook.plugin.ItemsAdderHook;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import me.untouchedodin0.privatemines.hook.plugin.WorldGuardHook;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.untouchedodin0.privatemines.hook.HookHandler.getHookHandler;

public class MineService {
    private final PrivateMines privateMines;
    private final MineFactory mineFactory;
    private final MineWorldManager mineWorldManager;
    private final HashMap<UUID, Mine> mines = Maps.newHashMap();

    @ConfigurationEntry(key = "should-create-wall-gap", section = "mine.configuration", value = "true", type = ConfigurationValueType.BOOLEAN)
    boolean shouldCreateWallGap;

    @ConfigurationEntry(key = "border-upgrade", section = "mine", value = "true", type = ConfigurationValueType.BOOLEAN)
    boolean borderUpgrade;

    @ConfigurationEntry(key = "upgrade", section = "materialChance", value = "obsidian", type = ConfigurationValueType.MATERIAL)
    Material upgradeMaterial;

    @ConfigurationEntry(key = "gap", section = "mine.configuration", value = "1", type = ConfigurationValueType.INT)
    int wallsGap;

    @ConfigurationEntry(key = "walls-go-up", section = "mine.configuration", value = "false", type = ConfigurationValueType.BOOLEAN)
    boolean wallsGoUp;

    public MineService(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.mineFactory = privateMines.getMineFactory();
        this.mineWorldManager = privateMines.getMineWorldManager();
        ConfigurationInstanceRegistry.registerInstance(this);
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

    public void create(UUID uuid) {
        create(uuid, privateMines.getMineTypeRegistry().getDefaultMineType());
    }

    public void create(UUID uuid, MineType mineType) {
        Mine mine = mineFactory.create(uuid, mineWorldManager.getNextFreeLocation(), mineType);
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

        // todo: handle oraxen/itemsadder blocks too.
        WeightedCollection<String> materials = mineType.materialChance();

        // todo: handle better
        // WeightedCollection<String> personalBlocks = mineType.personalBlocks();

        PrivateMineResetEvent privateMineResetEvent = new PrivateMineResetEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineResetEvent);
        if (privateMineResetEvent.isCancelled()) return;
        if (materials != null && materials.isEmpty()) return;

        WorldEditHook.WorldEditWorldIO worldEditWorldWriter = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        wallsGap = shouldCreateWallGap ? wallsGap : 0;
        worldEditWorldWriter.fill(boundingBox, wallsGap, materials);
    }

    private void handleMineDeletion(Mine mine) {
        MineData mineData = mine.getMineData();
        PrivateMineDeleteEvent privateMineDeleteEvent = new PrivateMineDeleteEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineDeleteEvent);
        if (privateMineDeleteEvent.isCancelled()) return;

        MineStructure structure = mineData.getMineStructure();
        BoundingBox schematicBoundingBox = structure.schematicBoundingBox();
        BoundingBox mineBoundingBox = structure.mineBoundingBox();

        getHookHandler().get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator()
                .removeMineRegions(mine);

        getHookHandler().get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO()
                .fill(schematicBoundingBox, 0, WorldEditHook.EMPTY);

        if (mineData.getMineType()
                .supportsPlugin(ItemsAdderHook.PLUGIN_NAME) && getHookHandler().hooked(ItemsAdderHook.PLUGIN_NAME)) {
            getHookHandler().get(ItemsAdderHook.PLUGIN_NAME, ItemsAdderHook.class)
                    .removeItemsAdderBlocksFromBoundingBox(mineBoundingBox);
        }

//        todo: make this better
//        databaseSomething.delete(mine);
//        SQLUtils.delete(mine);
    }

    public void upgrade(Mine mine) {
        MineTypeRegistry mineTypeRegistry = PrivateMines.inst().getMineTypeRegistry();
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

        File schematicFile = nextType.schematicFile();
        if (currentType.schematicFile().equals(schematicFile)) return;
        privateMines.getEconomy().withdrawPlayer(player, nextType.upgradeCost());

        // todo: handle this better.
        String mineRegionName = String.format("mine-%s", player.getUniqueId());
        String fullRegionName = String.format("full-mine-%s", player.getUniqueId());
        BoundingBox schematicArea = mineData.getMineStructure().schematicBoundingBox();

        WorldIO worldIO = getHookHandler().get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class).getWorldEditWorldIO();
        worldIO.fill(schematicArea, 0, WorldEditHook.EMPTY);

        BoundingBox schematicRegion = worldIO.placeSchematic(schematicFile, mine.getMineLocation());

        SchematicStorage schematicStorage = privateMines.getSchematicStorage();
        MineBlocks mineBlocks = schematicStorage.get(schematicFile);

        Location spawn = mine.getMineLocation().clone().add(0, 0, 1);
        BoundingBox mineRegion = mineBlocks.miningArea();

        RegionOrchestrator regionOrchestrator = HookHandler.getHookHandler()
                .get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator();

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
        reset(mine);
        teleportToSafety(mine);
    }

    private void teleportToSafety(Mine mine) {
        MineData mineData = mine.getMineData();
        World minesWorld = PrivateMines.inst().getMineWorldManager().getMinesWorld();
        BoundingBox mineBoundingBox = mineData.getMineStructure().mineBoundingBox();

        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean isPlayerInRegion = mineBoundingBox.contains(online.getLocation().toVector());
            boolean inWorld = online.getWorld().equals(minesWorld);
            if (isPlayerInRegion && inWorld) mine.teleport(online);
        }
    }


    public void expand(Mine mine, int amount) {
        if (!canExpand(mine, amount)) return;

        MineData mineData = mine.getMineData();
        MineStructure mineStructure = mineData.getMineStructure();
        BoundingBox originalMineBoundingBox = mineStructure.mineBoundingBox();

        BoundingBox airFillRegion = originalMineBoundingBox.clone();
        BoundingBox wallsRegion = originalMineBoundingBox.clone();
        BoundingBox mineBoundingBox = originalMineBoundingBox.clone();

        mineBoundingBox.expand(1);
        wallsRegion.expand(1);

        if (wallsGoUp) {
            wallsRegion.expand(-1);
        } else {
            wallsRegion.expand(-1, 0, -1);
        }

        airFillRegion.expand(1, 1, 1);

        PrivateMineExpandEvent privateMineExpandEvent = new PrivateMineExpandEvent(mine, mineBoundingBox);
        Bukkit.getPluginManager().callEvent(privateMineExpandEvent);
        if (privateMineExpandEvent.isCancelled()) return;

        WorldEditHook.WorldEditWorldIO worldEditWorldWriter = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        worldEditWorldWriter.fill(wallsRegion, 0, WeightedCollection.single(Material.AIR.name(), 1d));
        worldEditWorldWriter.fill(airFillRegion, 0, WeightedCollection.single(Material.AIR.name(), 1d));

        BoundingBox schematicBoundingBox = mineStructure.schematicBoundingBox().clone();
        schematicBoundingBox.expand(1, 1, 1);

        MineStructure newMineStructure = new MineStructure(
                mineBoundingBox,
                schematicBoundingBox,
                mineStructure.mineLocation(),
                mineStructure.spawnLocation()
        );

        WeightedCollection<String> materials = mineData.getMineType().materialChance();
        String mineRegionName = String.format("mineRegion-%s", mineData.getMineOwner());
        HookHandler.getHookHandler()
                .get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator()
                .addRegion(mineRegionName, mineBoundingBox);

        mineData.setMineStructure(newMineStructure);
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
                    Location location = new Location(mineWorldManager.getMinesWorld(), x, y, z);
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
        if (!mineWorldManager.getMinesWorld().equals(location.getWorld())) {
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
