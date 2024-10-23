package me.untouchedodin0.privatemines.mine;

import com.google.common.collect.Maps;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.UUID;

public class MineService {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
    private static final MineWorldManager MINE_WORLD_MANAGER = PRIVATE_MINES.getMineWorldManager();
    private final HashMap<UUID, Mine> mines = Maps.newHashMap();

    public void cache(Mine mine) {
        mines.put(mine.getMineData().getMineOwner(), mine);
    }

    public boolean uncache(Mine mine) {
        return uncache(mine.getMineData().getMineOwner());
    }

    public boolean uncache(UUID uuid) {
        Mine mine = mines.remove(uuid);
        return mine != null;
    }

    public void create(Mine mine) {
        cache(mine);
        // todo handle mine creation
    }

    public void delete(Mine mine) {
        uncache(mine);
        // todo handle mine deletion
    }

    public boolean has(UUID uuid) {
        return mines.containsKey(uuid);
    }

    public Mine get(UUID uuid) {
        return mines.get(uuid);
    }

    public Mine getNearest(Location location) {
        if (!MINE_WORLD_MANAGER.getMinesWorld().equals(location.getWorld())) {
            throw new RuntimeException("Invalid world fetching");
        }

        Mine nearest = null;
        for (Mine mine : mines.values()) {
            if (nearest == null || distanceToMine(location, mine) < distanceToMine(location, nearest)) {
                nearest = mine;
            }
        }

        return nearest;
    }

    private double distanceToMine(Location location, Mine mine) {
        return location.distance(mine.getMineLocation());
    }

}
