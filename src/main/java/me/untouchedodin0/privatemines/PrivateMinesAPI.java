package me.untouchedodin0.privatemines;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.Location;

import java.util.UUID;

public interface PrivateMinesAPI {

    boolean hasMine(UUID uuid);

    void createMine(UUID uuid);
    
    Mine getMine(UUID ownerUUID);

    Mine getNearestMine(Location location);
}
