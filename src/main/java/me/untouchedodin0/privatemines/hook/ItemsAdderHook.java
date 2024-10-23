package me.untouchedodin0.privatemines.hook;

public class ItemsAdderHook extends Hook {
    @Override
    public String getPluginName() {
        return "ItemsAdder";
    }

    @Override
    public void hook() {
        String itemsAdderVersion = getPlugin().getPluginMeta().getVersion();
        PLUGIN_INSTANCE.logInfo(String.format("""
                
                Found ItemsAdder v%s installed,
                make sure to list the materials under the itemsadder section
                within each mine type if you want to use itemsadder
                blocks or it won't load correctly!""", itemsAdderVersion));

    }
}
