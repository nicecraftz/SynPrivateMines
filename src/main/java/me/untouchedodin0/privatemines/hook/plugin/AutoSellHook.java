package me.untouchedodin0.privatemines.hook.plugin;

import me.clip.autosell.events.AutoSellEvent;
import me.clip.autosell.events.SellAllEvent;
import me.untouchedodin0.privatemines.hook.Hook;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AutoSellHook extends Hook implements Listener {
    private static final MineService MINE_WORLD_MANAGER = PLUGIN_INSTANCE.getMineService();
    private static final Economy ECONOMY = PLUGIN_INSTANCE.getEconomy();

    @Override
    public String getPluginName() {
        return "AutoSell";
    }

    @Override
    public void hook() {
        PLUGIN_INSTANCE.getServer().getPluginManager().registerEvents(this, PLUGIN_INSTANCE);
    }

    private boolean isValidEvent(Player player, Location location) {
        return player.getWorld()
                .equals(MINE_WORLD_MANAGER.getMinesWorld()) && MINE_WORLD_MANAGER.getNearest(location) != null;
    }

    private double applyTax(Player eventPlayer, Mine mine, double totalCost) {
        double tax = (totalCost / 100.0) * mine.getMineSettings().getTax();
        double afterTax = totalCost - tax;

        OfflinePlayer ownerOfflinePlayer = Bukkit.getOfflinePlayer(mine.getOwner());
        if (eventPlayer.getUniqueId().equals(ownerOfflinePlayer.getUniqueId())) return afterTax;

        ECONOMY.depositPlayer(ownerOfflinePlayer, tax);
        Player owner = ownerOfflinePlayer.getPlayer();
        if (owner == null || !owner.isOnline()) return afterTax;
        owner.sendRichMessage("<green>You've received $" + tax + " in taxes from " + eventPlayer.getName() + "!");
        return afterTax;
    }

    @EventHandler
    public void sellAll(SellAllEvent sellAllEvent) {
        Player eventPlayer = sellAllEvent.getPlayer();
        Location location = eventPlayer.getLocation();

        if (!isValidEvent(eventPlayer, location)) return;

        Mine mine = MINE_WORLD_MANAGER.getNearest(location);
        double totalCost = sellAllEvent.getTotalCost();
        double afterTax = applyTax(eventPlayer, mine, totalCost);
        sellAllEvent.setTotalCost(afterTax);
    }

    @EventHandler
    public void onAutoSell(AutoSellEvent autoSellEvent) {
        Player eventPlayer = autoSellEvent.getPlayer();
        Location location = eventPlayer.getLocation();

        if (!isValidEvent(eventPlayer, location)) return;

        Mine mine = MINE_WORLD_MANAGER.getNearest(location);
        double price = autoSellEvent.getPrice();
        double afterTax = applyTax(eventPlayer, mine, price);
        autoSellEvent.setMultiplier(afterTax);
    }
}
