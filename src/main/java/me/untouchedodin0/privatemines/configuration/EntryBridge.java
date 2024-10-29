package me.untouchedodin0.privatemines.configuration;

import org.bukkit.plugin.java.JavaPlugin;

public interface EntryBridge<T> {

    default T get(JavaPlugin javaPlugin, String path, T defaultValue) {
        T value = get(javaPlugin, path);
        return value == null ? defaultValue : value;
    }

    T get(JavaPlugin javaPlugin, String path);

    T fallback(String value);
}
