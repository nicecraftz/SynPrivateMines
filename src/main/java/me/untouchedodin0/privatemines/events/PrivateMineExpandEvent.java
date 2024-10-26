package me.untouchedodin0.privatemines.events;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.event.HandlerList;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class PrivateMineExpandEvent extends MineEvent {
    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final BoundingBox mineBoundingBox;

    public PrivateMineExpandEvent(Mine mine, BoundingBox mineBoundingBox) {
        super(mine);
        this.mineBoundingBox = mineBoundingBox;
    }

    public BoundingBox getMineBoundingBox() {
        return mineBoundingBox;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
