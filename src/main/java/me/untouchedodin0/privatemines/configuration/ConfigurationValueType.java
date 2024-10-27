package me.untouchedodin0.privatemines.configuration;

import me.untouchedodin0.privatemines.configuration.bridge.*;

public enum ConfigurationValueType {
    STRING(new StringConfigurationEntryBridge()),
    INT(new IntConfigurationEntryBridge()),
    BOOLEAN(new BooleanConfigurationEntryBridge()),
    DOUBLE(new DoubleConfigurationEntryBridge()),
    LONG(new LongConfigurationEntryBridge()),
    MATERIAL(new MaterialConfigurationEntryBridge()),
    STRING_LIST(new StringListConfigurationEntryBridge()),
    PARTICLE(new ParticleConfigurationEntryBridge()),
    COMPONENT(new ComponentConfigurationEntryBridge());

    private final ConfigurationEntryBridge<?> bridge;

    ConfigurationValueType(ConfigurationEntryBridge<?> bridge) {
        this.bridge = bridge;
    }

    public <T extends ConfigurationEntryBridge> ConfigurationEntryBridge<T> getBridge(Class<T> clazz) {
        return clazz.cast(bridge);
    }

    public ConfigurationEntryBridge<?> getBridge() {
        return bridge;
    }
}
