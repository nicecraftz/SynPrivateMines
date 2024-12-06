package me.untouchedodin0.privatemines.mine;

import lombok.Getter;
import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.Entry;
import me.untouchedodin0.privatemines.events.PrivateMineCreationEvent;
import me.untouchedodin0.privatemines.events.PrivateMineDeleteEvent;
import me.untouchedodin0.privatemines.events.PrivateMineExpandEvent;
import me.untouchedodin0.privatemines.events.PrivateMineResetEvent;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.hook.WorldIO;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import me.untouchedodin0.privatemines.hook.plugin.WorldGuardHook;
import me.untouchedodin0.privatemines.mine.world.EmptyWorldGenerator;
import me.untouchedodin0.privatemines.template.MineStructure;
import me.untouchedodin0.privatemines.template.MineTemplate;
import me.untouchedodin0.privatemines.template.MineTemplateRegistry;
import me.untouchedodin0.privatemines.template.SchematicTemplate;
import me.untouchedodin0.privatemines.utils.WeightedCollection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.untouchedodin0.privatemines.hook.HookHandler.getHookHandler;

public class MineService {
    private final Map<UUID, Mine> mines = new HashMap<>();
    private final MineTemplateRegistry mineTemplateRegistry;
    @Getter private final World minesWorld;

    @Entry(key = "distance", section = "mine", value = "150", type = ConfigurationValueType.INT) private int borderDistance;

    @Entry(key = "y-level", section = "mine", value = "50", type = ConfigurationValueType.INT) private int yLevel;

    @Entry(key = "border-upgrade", section = "mine", value = "true", type = ConfigurationValueType.BOOLEAN) private boolean borderUpgrade;

    @Entry(key = "upgrade", section = "materials", value = "obsidian", type = ConfigurationValueType.MATERIAL) private Material upgradeMaterial;

    @Entry(key = "walls-go-up", section = "mine", value = "false", type = ConfigurationValueType.BOOLEAN) private boolean wallsGoUp;

    @Entry(key = "is-closed-by-default", section = "mine", value = "false", type = ConfigurationValueType.BOOLEAN) private boolean defaultClosed;

    public MineService(MineTemplateRegistry mineTemplateRegistry) {
        this.mineTemplateRegistry = mineTemplateRegistry;
        ConfigurationInstanceRegistry.registerInstance(this);
        minesWorld = initializeWorld();
    }

    public Mine get(UUID uuid) {
        return mines.get(uuid);
    }

    public void cache(Mine mine) {
        mines.put(mine.getOwner(), mine);
    }

    public void uncache(Mine mine) {
        mines.remove(mine.getOwner());
    }

    public boolean has(UUID playerUniqueId) {
        return mines.containsKey(playerUniqueId);
    }

    public Map<UUID, Mine> getMines() {
        return Map.copyOf(mines);
    }

    private World initializeWorld() {
        World world = Bukkit.getWorld("privatemines");
        if (world == null) world = Bukkit.createWorld(new WorldCreator("privatemines").type(WorldType.FLAT)
                .generator(new EmptyWorldGenerator()));

        return world;
    }

    public Location getNextFreeLocation() {
        int minesCount = mines.size();
        int newMineDistanceFromCenter = borderDistance + (minesCount * 2);
        Location location = new Location(minesWorld, 0, yLevel, newMineDistanceFromCenter);
        return location;
    }

    public Mine getNearest(Location location) {
        if (!minesWorld.equals(location.getWorld())) throw new RuntimeException("Invalid world fetching");

        Mine nearest = null;
        for (Mine mine : mines.values()) {
            if (nearest == null || distanceToMine(location, mine) < distanceToMine(location, nearest)) nearest = mine;
        }

        return nearest;
    }

    public void create(UUID ownerUUID) {
        if (!mineTemplateRegistry.hasDefaultTemplate()) return;
        MineTemplate mineTemplate = mineTemplateRegistry.getDefaultTemplate();
        Mine mine = create(ownerUUID, mineTemplate);
        cache(mine);
    }

    private Mine create(UUID uuid, MineTemplate template) {
        WorldIO worldIO = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        Location location = getNextFreeLocation();
        SchematicTemplate schematicTemplate = template.schematicTemplate();
        worldIO.placeSchematic(schematicTemplate.getSchematicFile(), location);
        Mine mine = new Mine(uuid, location, template);

        MineStructure mineStructure = mine.getMineStructure();
        mine.getMineSettings().setOpen(!defaultClosed);

        Block spawnBlock = mineStructure.spawn().toLocation(minesWorld).getBlock();
        spawnBlock.setType(Material.AIR);

        PrivateMineCreationEvent creationEvent = new PrivateMineCreationEvent(mine);
        Bukkit.getPluginManager().callEvent(creationEvent);

        RegionOrchestrator regionOrchestrator = HookHandler.getHookHandler()
                .get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator();

        regionOrchestrator.createMineFlaggedRegions(mine);
        reset(mine);
        return mine;
    }

    public void reset(Mine mine) {
        teleportToSafety(mine);

        MineStructure mineStructure = mine.getMineStructure();
        WeightedCollection<String> materials = mine.getMineTemplate().materials();
        if (materials != null && materials.isEmpty()) return;

        PrivateMineResetEvent privateMineResetEvent = new PrivateMineResetEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineResetEvent);
        if (privateMineResetEvent.isCancelled()) return;

        WorldIO worldIO = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        worldIO.fill(mineStructure.mineArea(), 0, materials);
    }

    public void delete(Mine mine) {
        PrivateMineDeleteEvent privateMineDeleteEvent = new PrivateMineDeleteEvent(mine);
        Bukkit.getPluginManager().callEvent(privateMineDeleteEvent);
        if (privateMineDeleteEvent.isCancelled()) return;
        deleteMineFromWorld(mine);
        uncache(mine);
    }

    public void expand(Mine mine, int amount) {
        if (!canExpand(mine, amount)) return;

        MineStructure mineStructure = mine.getMineStructure();

        BoundingBox mineBoundingBox = mineStructure.mineArea();
        BoundingBox airFillRegion = mineStructure.mineArea().expand(1, 1, 1);
        BoundingBox wallsRegion = mineStructure.mineArea().expand(1);

        mineBoundingBox.expand(1);
        wallsRegion.expand(-1, wallsGoUp ? 1 : 0, wallsGoUp ? 1 : -1);

        PrivateMineExpandEvent expandEvent = new PrivateMineExpandEvent(mine, mineBoundingBox);
        Bukkit.getPluginManager().callEvent(expandEvent);
        if (expandEvent.isCancelled()) return;

        WorldEditHook.WorldEditWorldIO worldEditor = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        worldEditor.fill(wallsRegion, 0, WeightedCollection.single(Material.AIR.name(), 1d));
        worldEditor.fill(airFillRegion, 0, WeightedCollection.single(Material.AIR.name(), 1d));

        BoundingBox schematicBoundingBox = mineStructure.schematicArea().expand(1, 1, 1);

        MineStructure newMineStructure = new MineStructure(
                mineStructure.spawn(),
                mineBoundingBox,
                schematicBoundingBox
        );

        mine.setMineStructure(newMineStructure);
        reset(mine);
    }

    public boolean canExpand(Mine mine, int amount) {
        BoundingBox mineBoundingBox = mine.getMineStructure().mineArea();
        mineBoundingBox.expand(amount);
        boolean canExpand = true;

        for (int x = (int) Math.floor(mineBoundingBox.getMinX()); x < Math.ceil(mineBoundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(mineBoundingBox.getMinX()); y < Math.ceil(mineBoundingBox.getMaxX()); y++) {
                for (int z = (int) Math.floor(mineBoundingBox.getMinX()); z < Math.ceil(mineBoundingBox.getMaxX()); z++) {
                    Location location = new Location(minesWorld, x, y, z);
                    Material material = location.getBlock().getType();
                    if (material != upgradeMaterial) continue;
                    canExpand = false;
                    if (borderUpgrade) {
                        LoggerUtil.warning("Border upgrade is not implemented yet");
//                        upgrade(mine);
                    }
                }
            }
        }

        return canExpand;
    }

    private double distanceToMine(Location location, Mine mine) {
        return location.distance(mine.getLocation());
    }

    private void deleteMineFromWorld(Mine mine) {
        MineStructure mineStructure = mine.getMineStructure();
        getHookHandler().get(WorldGuardHook.PLUGIN_NAME, WorldGuardHook.class)
                .getRegionOrchestrator()
                .removeMineRegions(mine);

        getHookHandler().get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO()
                .fill(mineStructure.schematicArea(), 0, WorldIO.EMPTY);
    }

    private void teleportToSafety(Mine mine) {
        BoundingBox mineBoundingBox = mine.getMineStructure().mineArea();
        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean isPlayerInRegion = mineBoundingBox.contains(online.getLocation().toVector());
            boolean inWorld = online.getWorld().equals(minesWorld);
            if (isPlayerInRegion && inWorld) mine.teleport(online);
        }
    }
}
