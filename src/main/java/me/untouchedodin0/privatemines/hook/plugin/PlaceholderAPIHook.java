package me.untouchedodin0.privatemines.hook.plugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineData;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.utils.QueueUtils;
import me.untouchedodin0.privatemines.utils.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceholderAPIHook extends Hook {
    private static final String NO_RESULT = "";

    private static final PrivateMines PLUGIN_INSTANCE = PrivateMines.getInstance();
    private static final QueueUtils QUEUE_UTILS = PLUGIN_INSTANCE.getQueueUtils();
    private static final MineService MINE_SERVICE = PLUGIN_INSTANCE.getMineService();

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }

    @Override
    public void hook() {
        new PlaceholderAPIHookExpansion().register();
        PLUGIN_INSTANCE.logInfo("PlaceholderAPI Hooked successfully!");
    }

    private class PlaceholderAPIHookExpansion extends PlaceholderExpansion {
        @Override
        public @NotNull String getAuthor() {
            return "UntouchedOdin0";
        }

        @Override
        public @NotNull String getIdentifier() {
            return "privatemines";
        }

        @Override
        public @NotNull String getVersion() {
            return "1.0.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
            if (!offlinePlayer.isOnline()) return null;
            Player player = offlinePlayer.getPlayer();
            UUID playerUniqueId = player.getUniqueId();

            Mine mine = MINE_SERVICE.get(playerUniqueId);
            Location location = player.getLocation();

            switch (params.toLowerCase()) {
                case "size":
                    return handleSizePlaceholder(mine);
                case "owner":
                    return handleOwnerPlaceholder(location);
                case "location":
                    return handleMineLocationPlaceholder(playerUniqueId);
                case "spawn":
                    return handleSpawnPlaceholder(playerUniqueId);
//                case "inqueue":
//                    return String.valueOf(QUEUE_UTILS.isInQueue(playerUniqueId));
                case "hasmine":
                    return String.valueOf(MINE_SERVICE.has(playerUniqueId));
            }

            return null;
        }

        private String handleSpawnPlaceholder(UUID playerUUID) {
            Mine mine = MINE_SERVICE.get(playerUUID);
            if (mine == null) return NO_RESULT;
            return SerializationUtil.locationToString(mine.getSpawnLocation());
        }

        private String handleMineLocationPlaceholder(UUID playerUUID) {
            Mine mine = MINE_SERVICE.get(playerUUID);
            if (mine == null) return NO_RESULT;
            return mine.getMineLocation().toString();
        }

        private String handleOwnerPlaceholder(Location location) {
            Mine closest = MINE_SERVICE.getNearest(location);
            if (closest == null) return NO_RESULT;

            MineData mineData = closest.getMineData();
            return mineData.getMineOwner().toString();
        }

        private String handleSizePlaceholder(Mine mine) {
            MineData mineData = mine.getMineData();
            BoundingBox boundingBox = mineData.getMineStructure().mineBoundingBox();
            return (boundingBox.getMaxZ() - boundingBox.getMinZ()) + "";
        }
    }
}
