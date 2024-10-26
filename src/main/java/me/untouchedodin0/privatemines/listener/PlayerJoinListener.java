package me.untouchedodin0.privatemines.listener;

import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.InstanceRegistry;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.utils.QueueUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final QueueUtils queueUtils;
    private final MineService mineService;

    @ConfigurationEntry(key = "first-join-creation", section = "mine", value = "true", type = ConfigurationValueType.BOOLEAN)
    private boolean giveMineOnFirstJoin;

    public PlayerJoinListener(PrivateMines privateMines) {
        this.queueUtils = privateMines.getQueueUtils();
        this.mineService = privateMines.getMineService();
        InstanceRegistry.registerInstance(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        if (!giveMineOnFirstJoin) return;
        Player player = playerJoinEvent.getPlayer();
        if (!mineService.has(player.getUniqueId())) return;

        Mine mine = mineService.get(player.getUniqueId());
        mine.startTasks();

        if (queueUtils.isInQueue(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You're already in the queue!");
            return;
        }

        queueUtils.claim(player);
    }
}
