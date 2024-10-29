package me.untouchedodin0.privatemines.mine;

import me.untouchedodin0.privatemines.template.MineTemplate;
import me.untouchedodin0.privatemines.template.SchematicPoints;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mine {
    private final List<UUID> bannedPlayers = new ArrayList<>();
    private final UUID owner;
    private final UUID uuid;

    private MineTemplate mineTemplate;

    private Location location; // internal use.
    private SchematicPoints schematicPoints;

    private double airPercentage;
    private boolean open;
    private double tax;

    public Mine(UUID owner, Location location, MineTemplate mineTemplate) {
        this.owner = owner;
        this.uuid = UUID.randomUUID();
        this.mineTemplate = mineTemplate;

        this.location = location;
        schematicPoints = mineTemplate.schematicTemplate().computedPoints().shift(location.toVector());
    }

    public void setLocation(Location location) {
        this.location = location;
        schematicPoints = mineTemplate.schematicTemplate().computedPoints().shift(location.toVector());
    }

    public void teleport(Player player) {
        player.teleportAsync(getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Location getLocation() {
        return location.clone();
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getOwner() {
        return owner;
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

    public MineTemplate getMineTemplate() {
        return mineTemplate;
    }

    public SchematicPoints getSchematicPoints() {
        return schematicPoints;
    }

    public double getAirPercentage() {
        return airPercentage;
    }

    public void setAirPercentage(double airPercentage) {
        this.airPercentage = airPercentage;
    }

    public void setSchematicPoints(SchematicPoints schematicPoints) {
        this.schematicPoints = schematicPoints;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public Location getSpawnLocation() {
        return schematicPoints.spawn().toLocation(location.getWorld()).clone();
    }
}
