package me.untouchedodin0.privatemines.template;

import me.untouchedodin0.privatemines.PrivateMines;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public final class SchematicTemplate {
    private final String id;
    private final File schematicFile;
    private final Map<String, Boolean> mineAreaFlags;
    private final Map<String, Boolean> schematicAreaFlags;
    private SchematicPoints computedPoints;

    public SchematicTemplate(String id, File schematicFile, Map<String, Boolean> mineAreaFlags, Map<String, Boolean> schematicAreaFlags, SchematicPoints computedPoints) {
        this.id = id;
        this.schematicFile = schematicFile;
        this.mineAreaFlags = mineAreaFlags;
        this.schematicAreaFlags = schematicAreaFlags;
        this.computedPoints = computedPoints;
    }

    public SchematicTemplate(String id, File schematicFile, Map<String, Boolean> mineAreaFlags, Map<String, Boolean> schematicAreaFlags) {
        this.id = id;
        this.schematicFile = schematicFile;
        this.mineAreaFlags = mineAreaFlags;
        this.schematicAreaFlags = schematicAreaFlags;
    }

    public static SchematicTemplate fromConfigurationSection(ConfigurationSection configurationSection) {
        Map<String, Boolean> mineAreaFlags = new HashMap<>();
        ConfigurationSection mineAreaConfig = configurationSection.getConfigurationSection("mine-area-flags");

        for (String key : mineAreaConfig.getKeys(false)) {
            mineAreaFlags.put(key, mineAreaConfig.getBoolean(key));
        }

        Map<String, Boolean> schematicAreaFlags = new HashMap<>();
        ConfigurationSection schematicAreaConfig = configurationSection.getConfigurationSection("schematic-area-flags");

        for (String key : schematicAreaConfig.getKeys(false)) {
            schematicAreaFlags.put(key, schematicAreaConfig.getBoolean(key));
        }

        String name = configurationSection.getName();
        String schematicFileName = configurationSection.getString("file");
        File schematicFile = new File(PrivateMines.SCHEMATIC_DIRECTORY, schematicFileName);
        return new SchematicTemplate(name, schematicFile, mineAreaFlags, schematicAreaFlags);
    }

    public String id() {
        return id;
    }

    public File schematicFile() {
        return schematicFile;
    }

    public Map<String, Boolean> mineAreaFlags() {
        return mineAreaFlags;
    }

    public Map<String, Boolean> schematicAreaFlags() {
        return schematicAreaFlags;
    }

    public SchematicPoints computedPoints() {
        return computedPoints;
    }

    public void setComputedPoints(SchematicPoints computedPoints) {
        this.computedPoints = computedPoints;
    }

    public boolean hasComputedPoints() {
        return computedPoints != null;
    }
}
