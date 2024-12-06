package me.untouchedodin0.privatemines.hook;

import me.untouchedodin0.privatemines.hook.plugin.*;

import java.util.HashMap;
import java.util.Map;

public class HookHandler {
    private static final Map<String, Hook> hooks = new HashMap<>();
    private static HookHandler hookHandler;

    private HookHandler() {
        add(new PlaceholderAPIHook());
        add(new VaultHook());
        add(new WorldGuardHook());
        add(new WorldEditHook());
        add(new AutoSellHook());
        add(new ItemsAdderHook());
    }


    public static HookHandler getHookHandler() {
        if (hookHandler == null) {
            hookHandler = new HookHandler();
        }

        return hookHandler;
    }

    public void registerHooks() {
        hooks.values().forEach(Hook::tryHook);
    }

    public void add(Hook hook) {
        hooks.put(hook.getPluginName().toLowerCase(), hook);
    }

    public boolean hooked(String name) {
        return hooks.containsKey(name.toLowerCase());
    }

    public <T extends Hook> T get(String hookIdentifier, Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookIdentifier.toLowerCase()));
    }
}
