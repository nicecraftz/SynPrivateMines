package me.untouchedodin0.privatemines.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.untouchedodin0.privatemines.PrivateMines;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Getter
@RequiredArgsConstructor
public final class SchematicTemplate {
    private final String id;
    private final File schematicFile;
    private final Map<String, Boolean> mineAreaFlags;
    private final Map<String, Boolean> schematicAreaFlags;
    @Setter private MineStructure computedPoints;

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

    public boolean hasComputedPoints() {
        return computedPoints != null;
    }
}
