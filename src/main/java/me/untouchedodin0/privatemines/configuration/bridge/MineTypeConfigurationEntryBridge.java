package me.untouchedodin0.privatemines.configuration.bridge;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;
import me.untouchedodin0.privatemines.hook.MineBlockHandler;
import me.untouchedodin0.privatemines.mine.MineType;
import me.untouchedodin0.privatemines.mine.WeightedCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MineTypeConfigurationEntryBridge implements ConfigurationEntryBridge<MineType> {
    @Override
    public MineType get(JavaPlugin javaPlugin, String path) {

//        default:
        //        file: defaultmine.schem
        //        reset:
        //        time: 5
        //        percentage: 15
        //        expand: 1
        //        upgradeCost: 1.0
        //        hooks:
        //        oraxen: false
        //        itemsAdder: false
        //        materials:
        //        cobblestone: 99.0
        //        oraxen_your_block_id: 0.5
        //        itemsadder_your_block_id: 0.5
        //        default-flags:
        //        block-break: true
        //        upc-enchants: true
        //        full-flags:
        //        block-place: false
//        prices:
        //        cobblestone: 1.0

        ConfigurationSection configurationSection = javaPlugin.getConfig().getConfigurationSection(path);
        String identifier = configurationSection.getName();

        String fileName = configurationSection.getString("file");
        File schematicFile = new File(PrivateMines.getInstance().getSchematicsDirectory(), fileName);

        if (!schematicFile.exists()) {
            throw new IllegalStateException("Schematic file does not exist:" + schematicFile);
        }

        int resetTime = configurationSection.getInt("reset.time");
        double resetPercentage = configurationSection.getDouble("reset.percentage");
        int expand = configurationSection.getInt("expand");
        double upgradeCost = configurationSection.getDouble("upgradeCost");
        boolean oraxen = configurationSection.getBoolean("hooks.oraxen");
        boolean itemsAdder = configurationSection.getBoolean("hooks.itemsAdder");
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
                resetPercentage, upgradeCost, expand,
                pluginHooks,
                materialChances,
                mineFlags,
                schematicFlags,
                priceMap
        );
    }

    @Override
    public MineType fallback(String value) {
        throw new IllegalStateException("Cannot fallback to a value on a MineTypeConfigurationEntryBridge");
    }
}
