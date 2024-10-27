package me.untouchedodin0.privatemines.utils.world;

import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import org.bukkit.*;

import static me.untouchedodin0.privatemines.utils.world.Direction.NORTH;

public class MineWorldManager {
    private final Location defaultLocation;
    private Location nextLocation;
    private int distance = 0;
    private Direction direction;

    private World minesWorld;

    @ConfigurationEntry(key = "distance", section = "mine", value = "150", type = ConfigurationValueType.INT)
    private int borderDistance;

    @ConfigurationEntry(key = "y-level", section = "mine", value = "50", type = ConfigurationValueType.INT)
    private int yLevel;


    // todo: rewrite this class, the best way to create mines is by slotting them,
    //  we can create a line long 500 mines then create a new mine by expanding on a new axis.

    public MineWorldManager() {
        ConfigurationInstanceRegistry.registerInstance(this);
        minesWorld = Bukkit.getWorld("privatemines");
        minesWorld = minesWorld == null ? Bukkit.createWorld(new WorldCreator("privatemines").type(WorldType.FLAT)
                .generator(new EmptyWorldGenerator())) : minesWorld;
        yLevel = yLevel > minesWorld.getMaxHeight() ? 50 : yLevel;
        defaultLocation = minesWorld.getSpawnLocation();
    }

    public Location getNextFreeLocation() {
        // todo handle this better!
//    Location sqlLocation = SQLUtils.getCurrentLocation();

        Location sqlLocation = new Location(minesWorld, 0, 0, 0);
        if (distance == 0) {
            distance++;
            return defaultLocation;
        }

        if (direction == null) {
            direction = NORTH;
        }
        if (sqlLocation == null) {
            sqlLocation = direction.addTo(defaultLocation, distance * borderDistance);
            return sqlLocation;
        } else {
            if (nextLocation == null) {
                this.nextLocation = getDefaultLocation();
            }

            switch (direction) {
                case NORTH -> nextLocation.subtract(0, 0, distance * borderDistance);
                case EAST -> nextLocation.add(distance * borderDistance, 0, 0);
                case SOUTH -> nextLocation.add(0, 0, distance * borderDistance);
                case WEST -> nextLocation.subtract(distance * borderDistance, 0, 0);
            }
        }
        return nextLocation;
    }

    public World getMinesWorld() {
        return minesWorld;
    }

    public Location getDefaultLocation() {
        return defaultLocation;
    }
}
