package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.EntryBridge;

public class DoubleEntryBridge implements EntryBridge<Double> {
    @Override
    public Double get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getDouble(path);
    }

    @Override
    public Double fallback(String value) {
        return Double.parseDouble(value);
    }
}
