package me.untouchedodin0.privatemines;

import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineType;
import org.bukkit.Location;

import java.util.UUID;

public interface PrivateMinesAPI {

    boolean hasMine(UUID uuid);

    CreateResult createMine(UUID uuid, Location location, MineType mineType);

    Mine getPlayerMine(UUID uuid);

    Mine getNearestMine(Location location);

    enum CreateResult {
        FAILED,
        SUCCESS,
        ALREADY_EXISTS
    }
}
