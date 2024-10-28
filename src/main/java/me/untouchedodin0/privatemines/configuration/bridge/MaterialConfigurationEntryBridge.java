package me.untouchedodin0.privatemines.configuration.bridge;

import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;

public class MaterialConfigurationEntryBridge implements ConfigurationEntryBridge<Material> {
    @Override
    public Material get(JavaPlugin javaPlugin, String path) {
        String materialName = javaPlugin.getConfig().getString(path);
        if (materialName == null || materialName.isEmpty()) return null;
        return Registry.MATERIAL.get(NamespacedKey.minecraft(materialName.toLowerCase()));
    }

    @Override
    public Material fallback(String value) {
        return Registry.MATERIAL.get(NamespacedKey.minecraft(value.toLowerCase()));
    }
}
