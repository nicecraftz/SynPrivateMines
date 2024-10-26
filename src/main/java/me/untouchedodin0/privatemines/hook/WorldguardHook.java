package me.untouchedodin0.privatemines.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineType;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Map;

public class WorldguardHook extends Hook {
    public static final String PLUGIN_NAME = "WorldGuard";
    private org.bukkit.World bukkitMinesWorld;
    private World worldGuardMinesWorld;

    private RegionContainer regionContainer;
    private RegionManager regionManager;
    private FlagRegistry flags;

    private RegionOrchestrator<ProtectedRegion, Flag> regionOrchestrator;

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

    public RegionOrchestrator<ProtectedRegion, Flag> getRegionOrchestrator() {
        return regionOrchestrator;
    }

    public class WorldGuardRegionOrchestrator implements RegionOrchestrator<ProtectedRegion, Flag> {

        @Override
        public void setFlag(ProtectedRegion region, Flag flag, boolean state) {
            String stateString = state ? "allow" : "deny";
            FlagContext regionContext = FlagContext.create().setInput(stateString).setObject("region", region).build();
            region.setFlag(flag, regionContext);
        }

        @Override
        public void setFlagsForMine(Mine mine) {
            String ownerUUIDString = mine.getMineData().getMineOwner().toString();
            ProtectedRegion mineRegion = regionManager.getRegion(String.format("mine-%s", ownerUUIDString));
            ProtectedRegion schematicRegion = regionManager.getRegion(String.format("full-mine-%s", ownerUUIDString));

            MineType mineType = mine.getMineData().getMineType();
            applyFlagStates(mineType.mineFlags(), mineRegion);
            applyFlagStates(mineType.schematicAreaFlags(), schematicRegion);
        }

        @Override
        public void removeRegion(String name) {
            regionManager.removeRegion(name);
        }

        @Override
        public void addRegion(String name, BoundingBox boundingBox) {
            Location min = boundingBox.getMin().toLocation(bukkitMinesWorld);
            Location max = boundingBox.getMax().toLocation(bukkitMinesWorld);

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                    name,
                    BukkitAdapter.asBlockVector(min),
                    BukkitAdapter.asBlockVector(max)
            );

            regionManager.addRegion(region);
        }

        private void applyFlagStates(Map<String, Boolean> flagStates, ProtectedRegion region) {
            for (Map.Entry<String, Boolean> flagStateEntry : flagStates.entrySet()) {
                String flag = flagStateEntry.getKey();
                Flag<?> matchFlag = Flags.fuzzyMatchFlag(flags, flag);
                setFlag(region, matchFlag, flagStateEntry.getValue());
            }
        }
    }

}
