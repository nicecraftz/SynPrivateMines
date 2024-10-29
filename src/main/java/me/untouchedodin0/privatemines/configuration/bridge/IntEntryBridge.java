package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.EntryBridge;

public class IntEntryBridge implements EntryBridge<Integer> {
    @Override
    public Integer get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getInt(path);
    }

    @Override
    public Integer fallback(String value) {
        return Integer.parseInt(value);
    }
}
