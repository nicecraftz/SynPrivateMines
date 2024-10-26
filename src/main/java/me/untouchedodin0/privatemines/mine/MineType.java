package me.untouchedodin0.privatemines.mine;

import org.bukkit.Material;

import java.io.File;
import java.util.Map;

public record MineType(
        String name,
        File schematicFile,
        int resetTime,
        int expand,
        double resetPercentage,
        double upgradeCost,
        boolean useOraxen,
        boolean useItemsAdder,
        Map<Material, Double> materials,
        int maxPlayers,
        int maxMineSize,
        Map<String, Boolean> mineFlags,
        Map<String, Boolean> schematicAreaFlags,
        Map<String, Double> prices
) {
}
