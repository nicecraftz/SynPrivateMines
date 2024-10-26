package me.untouchedodin0.privatemines;

import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.mine.MineType;
import org.bukkit.Location;

import java.util.UUID;

public class PrivateMinesAPIImpl implements PrivateMinesAPI {
    private final MineService mineService;

    public PrivateMinesAPIImpl(MineService mineService) {
        this.mineService = mineService;
    }

    @Override
    public boolean hasMine(UUID uuid) {
        return mineService.has(uuid);
    }

    @Override
    public void createMine(UUID uuid, Location location, MineType mineType) {
        mineService.create(uuid, location, mineType);
    }

    @Override
    public Mine getPlayerMine(UUID uuid) {
        return mineService.get(uuid);
    }

    @Override
    public Mine getNearestMine(Location location) {
        return mineService.getNearest(location);
    }
}
