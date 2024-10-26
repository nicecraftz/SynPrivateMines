package me.untouchedodin0.privatemines.mine;

import java.util.HashMap;
import java.util.Map;

public class MineTypeRegistry {
    private final HashMap<String, MineType> mineRegistry = new HashMap<>();
    private MineType defaultMineType = null;

    public void register(MineType mineType) {
        mineRegistry.put(mineType.name().toLowerCase(), mineType);
    }

    public MineType get(String name) {
        return mineRegistry.get(name.toLowerCase());
    }

    public void setDefaultMineType(MineType defaultMineType) {
        this.defaultMineType = defaultMineType;
    }

    public boolean isDefaultMineSet() {
        return defaultMineType != null;
    }

    public MineType getDefaultMineType() {
        return defaultMineType;
    }

    public MineType getNextMineType(MineType current) {
        for (Map.Entry<String, MineType> entry : mineRegistry.entrySet()) {
            if (entry.getValue().equals(current)) {
                return mineRegistry.entrySet()
                        .stream()
                        .skip(mineRegistry.entrySet().stream().toList().indexOf(entry) + 1)
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse(current);
            }
        }

        return null;
    }

    public Map<String, MineType> getMineRegistry() {
        return Map.copyOf(mineRegistry);
    }
}
