package me.untouchedodin0.privatemines;

public class LoggerUtil {
    private static final PrivateMines PRIVATE_MINES = PrivateMines.inst();

    public static void info(String message) {
        PRIVATE_MINES.getLogger().info(message);
    }

    public static void info(String message, Object... objects) {
        PRIVATE_MINES.getLogger().info(String.format(message, objects));
    }

    public static void warning(String message) {
        PRIVATE_MINES.getLogger().warning(message);
    }

    public static void warning(String message, Object... objects) {
        PRIVATE_MINES.getLogger().warning(message);
    }

    public static void severe(String message) {
        PRIVATE_MINES.getLogger().severe(message);
    }

    public static void severe(String message, Exception exception) {
        PRIVATE_MINES.getLogger().severe(message + exception.getMessage());
    }

    public static void severe(String message, Object... objects) {
        PRIVATE_MINES.getLogger().severe(String.format(message, objects));
    }
}
