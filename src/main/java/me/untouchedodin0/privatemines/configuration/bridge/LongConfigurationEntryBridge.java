package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;

public class LongConfigurationEntryBridge implements ConfigurationEntryBridge<Long> {
    @Override
    public Long get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getLong(path);
    }

    @Override
    public Long fallback(String value) {
        return Long.parseLong(value);
    }
}
