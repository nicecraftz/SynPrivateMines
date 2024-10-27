package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.PrivateMines;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public abstract class Hook {
    protected static final PrivateMines PLUGIN_INSTANCE = PrivateMines.inst();

    public abstract String getPluginName();

    public void tryHook() {
        PrivateMines privateMines = PrivateMines.inst();
        if (canHook()) hook();
        else privateMines.logInfo("Tried to hook into " + getPluginName() + " but it wasn't found!");
    }

    private boolean canHook() {
        Plugin plugin = getPlugin();
        return plugin != null && plugin.isEnabled();
    }

    protected Plugin getPlugin() {
        PrivateMines privateMines = PrivateMines.inst();
        PluginManager pluginManager = privateMines.getServer().getPluginManager();
        Plugin plugin = pluginManager.getPlugin(getPluginName());
        return plugin;
    }

    public abstract void hook();
}
