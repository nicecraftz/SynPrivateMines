package me.untouchedodin0.privatemines.utils.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineType;
import me.untouchedodin0.privatemines.utils.regions.RegionOrchestrator;

import java.util.Map;

public class WorldguardHook extends Hook {
    private RegionManager regionManager;
    private FlagRegistry flags;

    @Override
    public String getPluginName() {
        return "WorldGuard";
    }

    @Override
    public void hook() {
        World worldGuardWorld = BukkitAdapter.adapt(PLUGIN_INSTANCE.getMineWorldManager().getMinesWorld());
        regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(worldGuardWorld);
        flags = WorldGuard.getInstance().getFlagRegistry();
    }

    private class WorldGuardRegionOrchestrator implements RegionOrchestrator<ProtectedRegion, Flag> {

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

        private void applyFlagStates(Map<String, Boolean> flagStates, ProtectedRegion region) {
            for (Map.Entry<String, Boolean> flagStateEntry : flagStates.entrySet()) {
                String flag = flagStateEntry.getKey();
                Flag<?> matchFlag = Flags.fuzzyMatchFlag(flags, flag);
                setFlag(region, matchFlag, flagStateEntry.getValue());
            }
        }
    }

}
