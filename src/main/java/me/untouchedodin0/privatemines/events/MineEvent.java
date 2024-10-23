package me.untouchedodin0.privatemines.events;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.UUID;

public abstract class MineEvent extends Event implements Cancellable {
    private final Mine mine;
    private final UUID owner;
    private boolean cancelled;

    public MineEvent(Mine mine, UUID owner) {
        this.mine = mine;
        this.owner = owner;
    }

    public Mine getMine() {
        return mine;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
