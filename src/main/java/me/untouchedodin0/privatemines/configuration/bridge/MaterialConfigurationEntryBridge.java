package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;

public class MaterialConfigurationEntryBridge implements ConfigurationEntryBridge<Material> {
    @Override
    public Material get(JavaPlugin javaPlugin, String path) {
        String materialName = javaPlugin.getConfig().getString(path);
        if (materialName == null || materialName.isEmpty()) return Material.STONE;
        return Registry.MATERIAL.get(NamespacedKey.minecraft(materialName));
    }

    @Override
    public Material fallback(String value) {
        return Registry.MATERIAL.get(NamespacedKey.minecraft(value.toLowerCase()));
    }
}
