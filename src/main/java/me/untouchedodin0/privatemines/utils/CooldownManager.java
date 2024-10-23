package me.untouchedodin0.privatemines.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<UUID, Integer> cooldowns = new HashMap<>();

    public void removeCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void setCooldown(UUID player, int time) {
        cooldowns.compute(player, (key, currentValue) -> (time < 1) ? 0 : time);
    }

    public int getCooldown(UUID player) {
        return cooldowns.getOrDefault(player, 0);
    }

}
