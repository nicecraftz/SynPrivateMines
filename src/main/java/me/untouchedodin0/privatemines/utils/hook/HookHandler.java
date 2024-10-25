package me.untouchedodin0.privatemines.utils.hook;

import java.util.HashSet;
import java.util.Set;

public class HookHandler {
    private static final Set<Hook> hooks = new HashSet<>() {{
        add(new PlaceholderAPIHook());
        add(new OraxenHook());
        add(new ItemsAdderHook());
        add(new VaultHook());
        add(new WorldguardHook());
    }};

    private HookHandler() {
    }

    public static void handleHooks() {
        hooks.forEach(Hook::tryHook);
    }

    public static boolean isHooked(String name) {

    }

}
