package me.untouchedodin0.privatemines.configuration.bridge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntryBridge;

public class ComponentConfigurationEntryBridge implements ConfigurationEntryBridge<Component> {

    @Override
    public Component get(JavaPlugin javaPlugin, String path) {
        return MiniMessage.miniMessage().deserialize(javaPlugin.getConfig().getString(path));
    }

    @Override
    public Component fallback(String value) {
        return MiniMessage.miniMessage().deserialize(value);
    }
}
