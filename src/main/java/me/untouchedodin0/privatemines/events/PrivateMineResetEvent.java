package me.untouchedodin0.privatemines.events;

import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PrivateMineResetEvent extends MineEvent {
    public static final HandlerList HANDLER_LIST = new HandlerList();

    public PrivateMineResetEvent(Mine mine) {
        super(mine);
    }


    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
