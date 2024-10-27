package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.privatemines.hook.MineBlockHandler;

import java.io.File;
import java.util.Map;

public record MineType(
        String name,
        File schematicFile,
        int resetTime,
        double resetPercentage,
        double upgradeCost,
        int expand,
        Map<String, MineBlockHandler> pluginSupport,
        WeightedCollection<String> materialChance,
        Map<String, Boolean> mineFlags,
        Map<String, Boolean> schematicAreaFlags,
        Map<String, Double> prices
) {
    public MineType {
        if (resetTime < 0) throw new IllegalArgumentException("Reset time cannot be negative");
        if (expand < 0) throw new IllegalArgumentException("Expand cannot be negative");
        if (resetPercentage < 0) throw new IllegalArgumentException("Reset percentage cannot be negative");
        if (upgradeCost < 0) throw new IllegalArgumentException("Upgrade cost cannot be negative");
    }

    public boolean supportsPlugin(String pluginName) {
        return pluginSupport.containsKey(pluginName.toLowerCase());
    }
}
