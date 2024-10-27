package me.untouchedodin0.privatemines.listener;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class ConnectionListener implements Listener {
    private final MineService mineService;

    @ConfigurationEntry(key = "first-join-creation", section = "mine", value = "true", type = ConfigurationValueType.BOOLEAN)
    private boolean giveMineOnFirstJoin;

    public ConnectionListener(PrivateMines privateMines) {
        this.mineService = privateMines.getMineService();
        ConfigurationInstanceRegistry.registerInstance(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (giveMineOnFirstJoin && !player.hasPlayedBefore()) {
            mineService.create(playerUniqueId);
            return;
        }
        
    }
}
