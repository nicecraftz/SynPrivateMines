package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.EntryBridge;

public class StringEntryBridge implements EntryBridge<String> {

    @Override
    public String get(JavaPlugin javaPlugin, String path) {
        return javaPlugin.getConfig().getString(path);
    }

    @Override
    public String fallback(String value) {
        return value;
    }
}
