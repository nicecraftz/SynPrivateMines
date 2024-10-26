package me.untouchedodin0.privatemines.hook.plugin;

import me.untouchedodin0.privatemines.hook.Hook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class VaultHook extends Hook {


    @Override
    public String getPluginName() {
        return "Vault";
    }

    @Override
    public void hook() {
        RegisteredServiceProvider registeredServiceProvider = getRegisteredServiceProvider();
        if (registeredServiceProvider == null) {
            PLUGIN_INSTANCE.logError(
                    "Couldn't find a suitable plugin to handle economy. Maybe install Essentials?",
                    new IllegalStateException("Economy Not Found.")
            );
            return;
        }

        Economy economy = (Economy) registeredServiceProvider.getProvider();
        PLUGIN_INSTANCE.setEconomy(economy);
    }

    private RegisteredServiceProvider<Economy> getRegisteredServiceProvider() {
        return getServer().getServicesManager().getRegistration(Economy.class);
    }
}
