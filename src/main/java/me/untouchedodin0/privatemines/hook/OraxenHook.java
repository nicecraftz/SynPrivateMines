package me.untouchedodin0.privatemines.hook;

public class OraxenHook extends Hook {
    @Override
    public String getPluginName() {
        return "Oraxen";
    }

    @Override
    public void hook() {
        String oraxenVersion = getPlugin().getPluginMeta().getVersion();
        PLUGIN_INSTANCE.logInfo(String.format("""
                Found Oraxen v%s installed,
                make sure to list the materials under the oraxen section
                within each mine type if you want to use oraxen
                blocks or it won't load correctly!""", oraxenVersion));
    }
}
