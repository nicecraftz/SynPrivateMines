package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;

public class BooleanConfigurationEntryBridge implements ConfigurationEntryBridge<Boolean> {
    @Override
    public Boolean get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getBoolean(path);
    }

    @Override
    public Boolean fallback(String value) {
        return Boolean.parseBoolean(value);
    }
}
