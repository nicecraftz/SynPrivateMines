package me.untouchedodin0.privatemines.configuration;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

public class ConfigurationInstanceRegistry {
    private static final Set<Object> registeredInstances = new HashSet<>();

    public static void registerInstance(Object object) {
        registeredInstances.add(object);
    }

    public static Set<Object> getRegisteredInstances() {
        return ImmutableSet.copyOf(registeredInstances);
    }
}
