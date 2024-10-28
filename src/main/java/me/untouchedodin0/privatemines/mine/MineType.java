package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.hook.MineBlockHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
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

    public boolean isDefault() {
        return name.equalsIgnoreCase("default");
    }

    public boolean supportsPlugin(String pluginName) {
        return pluginSupport.containsKey(pluginName.toLowerCase());
    }

    public static MineType fromConfigurationSection(ConfigurationSection configurationSection) {
        String identifier = configurationSection.getName();

        String fileName = configurationSection.getString("file");
        File schematicFile = new File(PrivateMines.inst().getSchematicsDirectory(), fileName);

        if (!schematicFile.exists()) {
            LoggerUtil.severe("Schematic file " + fileName + " does not exist for mine type " + identifier);
            return null;
        }

        int resetTime = configurationSection.getInt("reset.time");
        double resetPercentage = configurationSection.getDouble("reset.percentage");
        int expand = configurationSection.getInt("expand");
        double upgradeCost = configurationSection.getDouble("upgradeCost");

        ConfigurationSection materials = configurationSection.getConfigurationSection("materials");
        WeightedCollection<String> materialChances = new WeightedCollection<>();
        for (String key : materials.getKeys(false)) {
            materialChances.add(materials.getDouble(key), key);
        }

        ConfigurationSection defaultFlags = configurationSection.getConfigurationSection("default-flags");
        Map<String, Boolean> mineFlags = new HashMap<>();

        for (String key : defaultFlags.getKeys(false)) {
            mineFlags.put(key, defaultFlags.getBoolean(key));
        }

        ConfigurationSection fullFlags = configurationSection.getConfigurationSection("full-flags");
        Map<String, Boolean> schematicFlags = new HashMap<>();

        for (String key : fullFlags.getKeys(false)) {
            schematicFlags.put(key, fullFlags.getBoolean(key));
        }

        ConfigurationSection prices = configurationSection.getConfigurationSection("prices");
        Map<String, Double> priceMap = new HashMap<>();

        for (String key : prices.getKeys(false)) {
            priceMap.put(key, prices.getDouble(key));
        }

        Map<String, MineBlockHandler> pluginHooks = new HashMap<>();

        return new MineType(
                identifier,
                schematicFile,
                resetTime,
                resetPercentage,
                upgradeCost,
                expand,
                pluginHooks,
                materialChances,
                mineFlags,
                schematicFlags,
                priceMap
        );
    }
}
