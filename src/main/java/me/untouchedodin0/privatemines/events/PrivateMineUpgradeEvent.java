package me.untouchedodin0.privatemines.events;

import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.template.MineTemplate;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PrivateMineUpgradeEvent extends MineEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final MineTemplate oldTemplate;
    private final MineTemplate newTemplate;

    public PrivateMineUpgradeEvent(Mine mine, MineTemplate oldTemplate, MineTemplate newType) {
        super(mine);
        this.oldTemplate = oldTemplate;
        this.newTemplate = newType;
    }

    public MineTemplate getOldTemplate() {
        return oldTemplate;
    }

    public MineTemplate getNewTemplate() {
        return newTemplate;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
