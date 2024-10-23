package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.PrivateMines;

import java.util.HashSet;
import java.util.Set;

public class HookHandler {
    private static final PrivateMines PLUGIN_INSTANCE = PrivateMines.getInstance();
    private static final Set<Hook> hooks = new HashSet<>() {{
        add(new PlaceholderAPIHook());
        add(new OraxenHook());
        add(new ItemsAdderHook());
        add(new VaultHook());
        add(new AutoSellHook());
    }};

    private HookHandler() {
    }

    public static void handleHooks() {
        hooks.forEach(Hook::tryHook);
    }

}
