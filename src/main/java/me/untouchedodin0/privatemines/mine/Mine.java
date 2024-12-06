package me.untouchedodin0.privatemines.mine;

import lombok.Getter;
import lombok.Setter;
import me.untouchedodin0.privatemines.template.MineStructure;
import me.untouchedodin0.privatemines.template.MineTemplate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mine {
    private final List<UUID> bannedPlayers = new ArrayList<>();

    @Getter private final UUID uuid;
    @Getter private final UUID owner;
    private Location location;

    @Getter private final MineTemplate mineTemplate;
    @Setter @Getter private MineStructure mineStructure;
    @Getter private final MineSettings mineSettings;

    @Setter
    @Getter
    private double airPercentage;

    public Mine(UUID owner, Location location, MineTemplate mineTemplate) {
        this.owner = owner;
        this.uuid = UUID.randomUUID();
        this.mineTemplate = mineTemplate;

        this.location = location;
        mineStructure = mineTemplate.getShiftedPoints(location.toVector());
        mineSettings = new MineSettings();
    }

    public void setLocation(Location location) {
        this.location = location;
        mineStructure = mineTemplate.getShiftedPoints(location.toVector());
    }

    public void teleport(Player player) {
        player.teleportAsync(getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean isBanned(UUID uuid) {
        return bannedPlayers.contains(uuid);
    }

    public void ban(UUID uuid) {
        if (uuid.equals(owner)) return;
        bannedPlayers.add(uuid);
    }

    public boolean unban(UUID uuid) {
        return bannedPlayers.remove(uuid);
    }

    public Location getSpawnLocation() {
        return mineStructure.getSpawnLocation(location);
    }
}
