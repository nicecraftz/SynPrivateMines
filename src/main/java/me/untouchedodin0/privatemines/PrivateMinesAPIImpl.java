package me.untouchedodin0.privatemines;

import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PrivateMinesAPIImpl implements PrivateMinesAPI {
    private final MineService mineService;

    public PrivateMinesAPIImpl( MineService mineService) {
        this.mineService = mineService;
    }

    @Override
    public boolean hasMine(UUID uuid) {
        return mineService.has(uuid);
    }

    @Override
    public void createMine(UUID ownerUUID) {
        mineService.create(ownerUUID);
    }

    @Override
    public Mine getMine(UUID ownerUUID) {
        return mineService.get(ownerUUID);
    }

    @Override
    public Mine getNearestMine(Location location) {
        return mineService.getNearest(location);
    }
}
