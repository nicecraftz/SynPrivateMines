package me.untouchedodin0.privatemines.template;

import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SchematicTemplateRegistry {
    private final Map<String, SchematicTemplate> templates = new HashMap<>();

    public boolean contains(String id) {
        return templates.containsKey(id.toLowerCase());
    }

    public void register(SchematicTemplate template) {
        if (!template.getSchematicFile().exists()) {
            LoggerUtil.warning("Schematic file for template " + template.getId() + " does not exist.");
            return;
        }
        
        computeSchematicPoints(template);
        if (!template.hasComputedPoints()) return;
        templates.put(template.getId().toLowerCase(), template);
    }

    public Optional<SchematicTemplate> get(String id) {
        return Optional.ofNullable(templates.get(id.toLowerCase()));
    }

    private void computeSchematicPoints(SchematicTemplate template) {
        MineStructure mineStructure = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO()
                .computeSchematicPoints(template.getSchematicFile());
        template.setComputedPoints(mineStructure);
    }

    public void loadAllTemplatesFromConfig(ConfigurationSection templatesSection) {
        for (String key : templatesSection.getKeys(false)) {
            ConfigurationSection templateSection = templatesSection.getConfigurationSection(key);
            SchematicTemplate template = SchematicTemplate.fromConfigurationSection(templateSection);
            register(template);
        }
    }
}
