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
import me.untouchedodin0.privatemines.template.MineStructure;
import me.untouchedodin0.privatemines.template.SchematicTemplate;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Map;

import static java.lang.String.format;

public class WorldGuardHook extends Hook {
    public static final String PLUGIN_NAME = "WorldGuard";

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
        bukkitMinesWorld = PLUGIN_INSTANCE.getMineService().getMinesWorld();
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
        public void createMineFlaggedRegions(Mine mine) {
            String ownerUUIDString = mine.getOwner().toString();
            SchematicTemplate schematicTemplate = mine.getMineTemplate().schematicTemplate();
            MineStructure mineStructure = mine.getMineStructure();

            createRegionWithFlags(
                    format(MINE_REGION_FORMAT, ownerUUIDString),
                    mineStructure.mineArea(),
                    schematicTemplate.getMineAreaFlags()
            );

            createRegionWithFlags(
                    format(FULL_MINE_REGION_FORMAT, ownerUUIDString),
                    mineStructure.schematicArea(),
                    schematicTemplate.getSchematicAreaFlags()
            );
        }

        @Override
        public void removeMineRegions(Mine mine) {
            String owner = mine.getOwner().toString();
            regionManager.removeRegion(format(MINE_REGION_FORMAT, owner));
            regionManager.removeRegion(format(FULL_MINE_REGION_FORMAT, owner));
        }

        private void createRegionWithFlags(String regionName, BoundingBox boundingBox, Map<String, Boolean> stringBooleanMap) {
            Location min = boundingBox.getMin().toLocation(bukkitMinesWorld);
            Location max = boundingBox.getMax().toLocation(bukkitMinesWorld);

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                    regionName,
                    BukkitAdapter.asBlockVector(min),
                    BukkitAdapter.asBlockVector(max)
            );

            applyFlagStates(stringBooleanMap, region);
            regionManager.addRegion(region);
        }

        private void applyFlagStates(Map<String, Boolean> flagStates, ProtectedRegion region) {
            for (Map.Entry<String, Boolean> flagStateEntry : flagStates.entrySet()) {
                String flagName = flagStateEntry.getKey();
                Flag<?> matchFlag = Flags.fuzzyMatchFlag(flags, flagName);
                if (matchFlag == null) continue;
                if (!(matchFlag instanceof StateFlag stateFlag)) return;
                StateFlag.State stateFlagState = flagStateEntry.getValue() ? StateFlag.State.ALLOW : StateFlag.State.DENY;
                region.setFlag(stateFlag, stateFlagState);
            }
        }
    }

}
