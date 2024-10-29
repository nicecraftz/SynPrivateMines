package me.untouchedodin0.privatemines.configuration;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class ConfigurationProcessor {
    private final JavaPlugin javaPlugin;

    public ConfigurationProcessor(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public void refreshFields() {
        for (Object registeredInstance : ConfigurationInstanceRegistry.getRegisteredInstances()) {
            Class<?> clazz = registeredInstance.getClass();

            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Entry.class)) continue;
                Entry entry = field.getAnnotation(Entry.class);

                field.setAccessible(true);

                EntryBridge bridge = entry.type().getBridge();
                String constructedPath = entry.section() + "." + entry.key();

                Object value = bridge.get(javaPlugin, constructedPath);
                if (value == null) value = bridge.fallback(entry.value());

                try {
                    field.set(registeredInstance, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
