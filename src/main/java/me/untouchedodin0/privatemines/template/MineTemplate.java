package me.untouchedodin0.privatemines.template;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.storage.WeightedCollection;
import org.bukkit.configuration.ConfigurationSection;

public record MineTemplate(
        String id,
        SchematicTemplate schematicTemplate,
        int resetTime,
        int resetPercentage,
        int expand,
        double upgradeCost,
        WeightedCollection<String> materials
) {

    public boolean hasSchematicTemplate() {
        return schematicTemplate != null;
    }

    public boolean isDefault() {
        return id.equalsIgnoreCase("default");
    }

    public static MineTemplate fromConfigurationSection(ConfigurationSection configurationSection) {
        WeightedCollection<String> materials = new WeightedCollection<>();
        ConfigurationSection materialsSection = configurationSection.getConfigurationSection("materials");

        for (String material : materialsSection.getKeys(false)) {
            double weight = materialsSection.getDouble(material, 1.0);
            materials.add(weight, material);
        }

        String schematic = configurationSection.getString("schematic");
        SchematicTemplateRegistry schematicTemplateRegistry = PrivateMines.inst().getSchematicTemplateRegistry();
        SchematicTemplate configSchematicTemplate = schematicTemplateRegistry.get(schematic).orElse(null);

        String name = configurationSection.getName();
        int resetTime = configurationSection.getInt("reset.time", 5);
        int resetPercentage = configurationSection.getInt("reset.percentage", 15);
        int expand = configurationSection.getInt("expand", 1);
        double upgradeCost = configurationSection.getDouble("upgrade-cost", 1.0);

        return new MineTemplate(
                name,
                configSchematicTemplate,
                resetTime,
                resetPercentage,
                expand,
                upgradeCost,
                materials
        );
    }

}
