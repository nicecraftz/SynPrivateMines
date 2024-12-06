package me.untouchedodin0.privatemines;

import me.untouchedodin0.privatemines.commands.AddonsCommand;
import me.untouchedodin0.privatemines.commands.PrivateMinesCommand;
import me.untouchedodin0.privatemines.configuration.ConfigurationProcessor;
import me.untouchedodin0.privatemines.data.database.Credentials;
import me.untouchedodin0.privatemines.data.database.Database;
import me.untouchedodin0.privatemines.data.database.MySQL;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.listener.ConnectionListener;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.template.MineTemplateRegistry;
import me.untouchedodin0.privatemines.template.SchematicTemplateRegistry;
import me.untouchedodin0.privatemines.utils.UpdateChecker;
import me.untouchedodin0.privatemines.utils.addon.AddonManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PrivateMines extends JavaPlugin {
    private static PrivateMines instance;
    private PrivateMinesAPI minesAPI;

    public static File SCHEMATIC_DIRECTORY;
    public static File addonsDirectory;

    private ConfigurationProcessor configurationProcessor;

    private SchematicTemplateRegistry schematicTemplateRegistry;
    private MineTemplateRegistry mineTemplateRegistry;

    private MineService mineService;

    private AddonManager addonManager;
    private Economy economy = null;

    private Database database;

    public static PrivateMines inst() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        LoggerUtil.info("Loading Private Mines v" + getPluginMeta().getVersion());
        saveDefaultConfig();
        esnureDirectoriesCreation();
        configurationProcessor = new ConfigurationProcessor(this);
        schematicTemplateRegistry = new SchematicTemplateRegistry();
        mineTemplateRegistry = new MineTemplateRegistry();
        mineService = new MineService(mineTemplateRegistry);

        Credentials credentials = Credentials.fromConfig(getConfig().getConfigurationSection("database"));
        database = new MySQL();

        HookHandler.getHookHandler().registerHooks();

        schematicTemplateRegistry.loadAllTemplatesFromConfig(getConfig().getConfigurationSection("schematics"));
        mineTemplateRegistry.loadAllTemplatesFromConfig(getConfig().getConfigurationSection("mines"));

        minesAPI = new PrivateMinesAPIImpl(mineService);
        addonManager = new AddonManager(this);

        new AddonsCommand(this).registerCommands();
        new PublicMinesCommand().registerCommands();
        new PrivateMinesCommand(this).registerCommands();

        getServer().getPluginManager().registerEvents(new ConnectionListener(mineService), this);
        new UpdateChecker(this).fetch();

        configurationProcessor.refreshFields();
    }

    private void esnureDirectoriesCreation() {
        SCHEMATIC_DIRECTORY = new File(getDataFolder(), "schematics");
        addonsDirectory = new File(getDataFolder(), "addons");
        if (!SCHEMATIC_DIRECTORY.exists()) SCHEMATIC_DIRECTORY.mkdirs();
        if (!addonsDirectory.exists()) SCHEMATIC_DIRECTORY.mkdirs();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configurationProcessor != null) configurationProcessor.refreshFields();
    }

    @Override
    public void onDisable() {
        LoggerUtil.info("PrivateMines is disabling..");
    }

    public PrivateMinesAPI getMinesAPI() {
        return minesAPI;
    }

    public MineService getMineService() {
        return mineService;
    }

    public void setEconomy(Economy economy) {
        this.economy = economy;
    }

    public Economy getEconomy() {
        return economy;
    }

    public File getAddonsDirectory() {
        return addonsDirectory;
    }

    public SchematicTemplateRegistry getSchematicTemplateRegistry() {
        return schematicTemplateRegistry;
    }

    public AddonManager getAddonManager() {
        return addonManager;
    }

    public MineTemplateRegistry getMineTemplateRegistry() {
        return mineTemplateRegistry;
    }
}
