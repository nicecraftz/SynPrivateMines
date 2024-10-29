package me.untouchedodin0.privatemines.hook.plugin;

import me.untouchedodin0.privatemines.LoggerUtil;
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
        RegisteredServiceProvider registeredServiceProvider = getServer().getServicesManager()
                .getRegistration(Economy.class);

        if (registeredServiceProvider == null) {
            LoggerUtil.severe("Couldn't find a suitable plugin to handle economy. Maybe install Essentials?");
            return;
        }

        Economy economy = (Economy) registeredServiceProvider.getProvider();
        PLUGIN_INSTANCE.setEconomy(economy);
    }

    @Override
    public boolean required() {
        return true;
    }
}
