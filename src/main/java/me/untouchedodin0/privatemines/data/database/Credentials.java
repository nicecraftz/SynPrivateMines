package me.untouchedodin0.privatemines.data.database;

import org.bukkit.configuration.ConfigurationSection;

public record Credentials(String username, String password, String host, String database, int port) {
    public static Credentials fromConfig(ConfigurationSection database) {
        return new Credentials(
                database.getString("username"),
                database.getString("password"),
                database.getString("host"),
                database.getString("database"),
                database.getInt("port")
        );
    }
}
