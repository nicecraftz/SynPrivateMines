package me.untouchedodin0.privatemines.template;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MineTemplateRegistry {
    private final Map<String, MineTemplate> templates = new HashMap<>();
    private MineTemplate defaultTemplate;

    public boolean hasDefaultTemplate() {
        return defaultTemplate != null;
    }

    public MineTemplate getDefaultTemplate() {
        return defaultTemplate;
    }

    public void register(MineTemplate template) {
        templates.put(template.id().toLowerCase(), template);
    }

    public Optional<MineTemplate> get(String id) {
        return Optional.ofNullable(templates.get(id.toLowerCase()));
    }

    public void loadAllTemplatesFromConfig(ConfigurationSection templatesSection) {
        for (String key : templatesSection.getKeys(false)) {
            ConfigurationSection templateSection = templatesSection.getConfigurationSection(key);
            MineTemplate template = MineTemplate.fromConfigurationSection(templateSection);
            if (!template.hasSchematicTemplate()) continue;
            if (template.isDefault()) {
                defaultTemplate = template;
            }
            register(template);
        }
    }
}
