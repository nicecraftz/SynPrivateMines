package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;

import java.util.List;

public class StringListConfigurationEntryBridge implements ConfigurationEntryBridge<List<String>> {
    @Override
    public List<String> get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getStringList(path);
    }

    @Override
    public List<String> fallback(String value) {
        return List.of(value.split(","));
    }
}
