package me.untouchedodin0.privatemines.configuration;

import com.google.common.collect.Sets;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Set;

public class ConfigurationProcessor {
    private final JavaPlugin javaPlugin;
    private final Set<Object> trackedObjects = Sets.newHashSet();

    public ConfigurationProcessor(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public void process(Object object) {
        if (trackedObjects.contains(object)) return;
        refreshFields(object);
        trackedObjects.add(object);
    }

    public void refresh() {
        for (Object object : trackedObjects) {
            refreshFields(object);
        }
    }

    private void refreshFields(Object object) {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigurationEntry.class)) continue;
            ConfigurationEntry entry = field.getAnnotation(ConfigurationEntry.class);

            field.setAccessible(true);

            ConfigurationEntryBridge bridge = entry.type().getBridge();
            String constructedPath = entry.section() + "." + entry.key();

            Object value = bridge.get(javaPlugin, constructedPath);

            if (value == null) value = bridge.fallback(entry.value());

            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}
