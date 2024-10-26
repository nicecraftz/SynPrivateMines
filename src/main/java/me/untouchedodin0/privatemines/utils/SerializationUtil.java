package me.untouchedodin0.privatemines.utils;


import org.bukkit.Location;

public class SerializationUtil {
    private static final String LOCATION_FORMAT = "%s-%s-%s";

    private SerializationUtil() {

    }


    public static String locationToString(Location location) {
        return String.format(LOCATION_FORMAT, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
