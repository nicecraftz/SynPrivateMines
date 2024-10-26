package me.untouchedodin0.privatemines.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldowns {
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static Cooldowns instance;

    private Cooldowns() {

    }

    public static Cooldowns getInstance() {
        if (instance == null) instance = new Cooldowns();
        return instance;
    }

    public static void remove(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public static void set(UUID player, int time) {
        cooldowns.compute(player, (key, currentValue) -> (time < 1) ? 0 : time);
    }

    public static int get(UUID player) {
        return cooldowns.getOrDefault(player, 0);
    }

}
