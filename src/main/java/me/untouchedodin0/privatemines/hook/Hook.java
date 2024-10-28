package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.LoggerUtil;
import me.untouchedodin0.privatemines.PrivateMines;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public abstract class Hook {
    protected static final PrivateMines PLUGIN_INSTANCE = PrivateMines.inst();

    public abstract String getPluginName();

    public void tryHook() {
        if (canHook()) {
            hook();
            LoggerUtil.info("Hooked into " + getPluginName() + " successfully!");
        } else LoggerUtil.warning("Could not hook into " + getPluginName() + " because it is not enabled.");
    }

    private boolean canHook() {
        Plugin plugin = getPlugin();
        return plugin != null && plugin.isEnabled();
    }

    protected Plugin getPlugin() {
        PluginManager pluginManager = PLUGIN_INSTANCE.getServer().getPluginManager();
        Plugin plugin = pluginManager.getPlugin(getPluginName());
        return plugin;
    }

    public abstract void hook();
}
