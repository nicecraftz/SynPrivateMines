package me.untouchedodin0.privatemines.hook.plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.hook.RegionOrchestrator;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineStructure;
import me.untouchedodin0.privatemines.mine.MineType;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Map;

import static java.lang.String.format;

public class WorldGuardHook extends Hook {
    public static final String PLUGIN_NAME = "WorldGuard";

    private static final String MINE_REGION_FORMAT = "mine-%s";
    private static final String FULL_MINE_REGION_FORMAT = "full-mine-%s";

    private org.bukkit.World bukkitMinesWorld;
    private World worldGuardMinesWorld;

    private RegionContainer regionContainer;
    private RegionManager regionManager;
    private FlagRegistry flags;

    private RegionOrchestrator regionOrchestrator;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void hook() {
        bukkitMinesWorld = PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld();
        worldGuardMinesWorld = BukkitAdapter.adapt(bukkitMinesWorld);

        regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        regionManager = regionContainer.get(worldGuardMinesWorld);

        flags = WorldGuard.getInstance().getFlagRegistry();
        regionOrchestrator = new WorldGuardRegionOrchestrator();
    }

    public RegionOrchestrator getRegionOrchestrator() {
        return regionOrchestrator;
    }

    public class WorldGuardRegionOrchestrator implements RegionOrchestrator {
        @Override
        public void createRegionsThenSetFlagsForMine(Mine mine) {
            String ownerUUIDString = mine.getMineData().getMineOwner().toString();
            MineStructure structure = mine.getMineData().getMineStructure();

            String mineRegionName = format(MINE_REGION_FORMAT, ownerUUIDString);
            addRegion(mineRegionName, structure.mineBoundingBox());

            String fullMineRegionName = format(FULL_MINE_REGION_FORMAT, ownerUUIDString);
            addRegion(fullMineRegionName, structure.schematicBoundingBox());

            ProtectedRegion mineRegion = regionManager.getRegion(mineRegionName);
            ProtectedRegion schematicRegion = regionManager.getRegion(fullMineRegionName);

            MineType mineType = mine.getMineData().getMineType();

            applyFlagStates(mineType.mineFlags(), mineRegion);
            applyFlagStates(mineType.schematicAreaFlags(), schematicRegion);
        }

        @Override
        public void removeMineRegions(Mine mine) {

        }

        @Override
        public void addRegion(String name, BoundingBox boundingBox) {
            Location min = boundingBox.getMin().toLocation(bukkitMinesWorld);
            Location max = boundingBox.getMax().toLocation(bukkitMinesWorld);

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(name,
                    BukkitAdapter.asBlockVector(min),
                    BukkitAdapter.asBlockVector(max)
            );

            regionManager.addRegion(region);
        }

        private void setFlag(ProtectedRegion region, Flag<?> flag, boolean state) {
            if (!(flag instanceof StateFlag stateFlag)) return;
            StateFlag.State stateFlagState = state ? StateFlag.State.ALLOW : StateFlag.State.DENY;
            region.setFlag(stateFlag, stateFlagState);
        }

        private void applyFlagStates(Map<String, Boolean> flagStates, ProtectedRegion region) {
            for (Map.Entry<String, Boolean> flagStateEntry : flagStates.entrySet()) {
                String flagName = flagStateEntry.getKey();
                Flag<?> matchFlag = Flags.fuzzyMatchFlag(flags, flagName);
                if (matchFlag == null) continue;
                setFlag(region, matchFlag, flagStateEntry.getValue());
            }
        }
    }

}
