package me.untouchedodin0.privatemines.utils.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.untouchedodin0.kotlin.mine.data.MineData;
import me.untouchedodin0.kotlin.mine.storage.MineStorage;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.utils.QueueUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.misc.LocationUtils;

import java.util.UUID;

public class PlaceholderAPIHook extends Hook {
    private static final String NO_RESULT = "";

    private static final PrivateMines PLUGIN_INSTANCE = PrivateMines.getInstance();
    private static final QueueUtils queueUtils = PLUGIN_INSTANCE.getQueueUtils();
    private static final MineStorage mineStorage = PLUGIN_INSTANCE.getMineStorage();

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

            Mine mine = mineStorage.get(playerUniqueId);
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
                case "inqueue":
                    return String.valueOf(queueUtils.isInQueue(playerUniqueId));
                case "hasmine":
                    return String.valueOf(mineStorage.hasMine(player));
            }

            return null;
        }

        private String handleSpawnPlaceholder(UUID playerUUID) {
            Mine mine = mineStorage.get(playerUUID);
            if (mine == null) return NO_RESULT;
            return LocationUtils.toString(mine.getSpawnLocation());
        }

        private String handleMineLocationPlaceholder(UUID playerUUID) {
            Mine mine = mineStorage.get(playerUUID);
            if (mine == null) return NO_RESULT;
            return mine.getLocation().toString();
        }

        private String handleOwnerPlaceholder(Location location) {
            Mine closest = mineStorage.getClosest(location);
            if (closest == null) return NO_RESULT;

            MineData mineData = closest.getMineData();
            return mineData.getMineOwner().toString();
        }

        private String handleSizePlaceholder(Mine mine) {
            MineData mineData = mine.getMineData();
            int minimum = mineData.getMinimumMining().getBlockZ();
            int maximum = mineData.getMaximumMining().getBlockZ();
            int distance = maximum - minimum;
            return distance + "";
        }
    }
}
