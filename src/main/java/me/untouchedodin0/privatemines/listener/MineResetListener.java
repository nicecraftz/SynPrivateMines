package me.untouchedodin0.privatemines.listener;

import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class MineResetListener implements Listener {
    private final MineService mineService;

    public MineResetListener(MineService mineService) {
        this.mineService = mineService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!mineService.has(playerUUID)) return;
        mineService.get(playerUUID).stopTasks();
    }
}
