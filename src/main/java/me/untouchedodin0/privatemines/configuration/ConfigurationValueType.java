package me.untouchedodin0.privatemines.configuration;

import me.untouchedodin0.privatemines.configuration.bridge.*;

public enum ConfigurationValueType {
    STRING(new StringEntryBridge()),
    INT(new IntEntryBridge()),
    BOOLEAN(new BooleanEntryBridge()),
    DOUBLE(new DoubleEntryBridge()),
    LONG(new LongEntryBridge()),
    MATERIAL(new MaterialEntryBridge()),
    STRING_LIST(new StringListEntryBridge()),
    PARTICLE(new ParticleEntryBridge()),
    COMPONENT(new ComponentEntryBridge());

    private final EntryBridge<?> bridge;

    ConfigurationValueType(EntryBridge<?> bridge) {
        this.bridge = bridge;
    }

    public <T extends EntryBridge> EntryBridge<T> getBridge(Class<T> clazz) {
        return clazz.cast(bridge);
    }

    public EntryBridge<?> getBridge() {
        return bridge;
    }
}
