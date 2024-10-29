package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.EntryBridge;

public class BooleanEntryBridge implements EntryBridge<Boolean> {
    @Override
    public Boolean get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getBoolean(path);
    }

    @Override
    public Boolean fallback(String value) {
        return Boolean.parseBoolean(value);
    }
}
