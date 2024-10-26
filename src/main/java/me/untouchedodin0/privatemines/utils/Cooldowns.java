package me.untouchedodin0.privatemines.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldowns {
    private static final Map<UUID, Cooldown> cooldowns = new HashMap<>();

    private Cooldowns() {

    }

    public static void remove(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public static void set(UUID player, int time) {
        cooldowns.compute(player, (key, currentValue) -> (time < 1) ? null : new Cooldown(player, time));
    }

    public static boolean hasExpiredCooldown(UUID uuid) {
        Cooldown cooldown = cooldowns.getOrDefault(uuid, new Cooldown(uuid, 0));
        return System.currentTimeMillis() - cooldown.timestamp() > cooldown.time() * 1000;
    }

    public static int get(UUID player) {
        return cooldowns.getOrDefault(player, new Cooldown(player, 0)).time();
    }

    private record Cooldown(UUID player, int time, long timestamp) {
        public Cooldown(UUID uuid, int time) {
            this(uuid, time, System.currentTimeMillis());
        }
    }
}
