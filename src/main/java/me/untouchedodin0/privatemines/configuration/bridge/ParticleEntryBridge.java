package me.untouchedodin0.privatemines.configuration.bridge;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;
import me.untouchedodin0.privatemines.configuration.EntryBridge;

public class ParticleEntryBridge implements EntryBridge<Particle> {
    @Override
    public Particle get(JavaPlugin javaPlugin, String path) {
        String particleName = javaPlugin.getConfig().getString(path);
        Particle particle = Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(particleName));
        return particle;
    }

    @Override
    public Particle fallback(String value) {
        Particle particle = Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(value));
        return particle;
    }
}
