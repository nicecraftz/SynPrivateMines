package me.untouchedodin0.privatemines.hook;

import java.util.HashMap;
import java.util.Map;

public class HookHandler {
    private static final Map<String, Hook> hooks = new HashMap<>() {{
        add(new PlaceholderAPIHook());
        add(new OraxenHook());
        add(new ItemsAdderHook());
        add(new VaultHook());
        add(new WorldguardHook());
    }};

    private HookHandler() {
    }

    public static void handleHooks() {
        hooks.values().forEach(Hook::tryHook);
    }

    public static void add(Hook hook) {
        hooks.put(hook.getPluginName().toLowerCase(), hook);
    }

    public static boolean hooked(String name) {
        return hooks.containsKey(name.toLowerCase());
    }

    public static <T extends Hook> T get(String hookIdentifier, Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookIdentifier.toLowerCase()));
    }
}
