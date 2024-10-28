package me.untouchedodin0.privatemines;

import me.untouchedodin0.privatemines.commands.AddonsCommand;
import me.untouchedodin0.privatemines.commands.PrivateMinesCommand;
import me.untouchedodin0.privatemines.commands.PublicMinesCommand;
import me.untouchedodin0.privatemines.configuration.ConfigurationProcessor;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.listener.ConnectionListener;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import me.untouchedodin0.privatemines.utils.UpdateChecker;
import me.untouchedodin0.privatemines.utils.addon.AddonManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PrivateMines extends JavaPlugin {
    private static PrivateMines instance;
    private PrivateMinesAPI minesAPI;

    private static final int PLUGIN_ID = 11413;

    private File schematicsDirectory;
    private File addonsDirectory;

    private ConfigurationProcessor configurationProcessor;

    private MineFactory mineFactory = null;
    private MineTypeRegistry mineTypeRegistry = null;
    private SchematicStorage schematicStorage = null;

    private MineService mineService = null;

    private AddonManager addonManager;
    private Economy economy = null;

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

        schematicStorage = new SchematicStorage();
        mineTypeRegistry = new MineTypeRegistry(schematicStorage);
        mineFactory = new MineFactory(schematicStorage);
        mineService = new MineService(this);

        HookHandler.getHookHandler().registerHooks();

        minesAPI = new PrivateMinesAPIImpl(mineService);
        addonManager = new AddonManager(this);

        registerMineTypes();

        new AddonsCommand(this).registerCommands();
        new PublicMinesCommand().registerCommands();
        new PrivateMinesCommand(this).registerCommands();

        getServer().getPluginManager().registerEvents(new ConnectionListener(mineService), this);

//        MineConfig.getMineTypes().forEach((s, mineType) -> mineTypeManager.registerMineType(mineType));
//        MineConfig.mineTypes.forEach((name, mineType) -> {
//
//            String fileName = mineType.getFile();
//            File schematicFile = new File(schematicsDirectory, fileName);
//
//            if (!schematicFile.exists()) {
//                logInfo(String.format("Schematic fileName for %s does not exist!", fileName));
//                return;
//            }
//
//            SchematicIterator.MineBlocks mineBlocks = schematicIterator.findRelativePoints(schematicFile);
//            schematicStorage.addSchematic(schematicFile, mineBlocks);
//        });

//        ensureDatabaseFileCreation();

        // TODO: rewrite whole sql data handle system.
//        sqLite = new SQLite();
//        sqLite.load();
//        sqlHelper = new SQLHelper(sqLite.getSQLConnection());

//        sqlHelper.executeUpdate("""
//                CREATE TABLE IF NOT EXISTS privatemines (
//                owner VARCHAR(36) UNIQUE NOT NULL,
//                mineType VARCHAR(10) NOT NULL,
//                mineLocation VARCHAR(30) NOT NULL,
//                corner1 VARCHAR(30) NOT NULL,
//                corner2 VARCHAR(30) NOT NULL,
//                fullRegionMin VARCHAR(30) NOT NULL,
//                fullRegionMax VARCHAR(30) NOT NULL,
//                spawn VARCHAR(30) NOT NULL,
//                tax FLOAT NOT NULL,
//                isOpen INT NOT NULL,
//                maxPlayers INT NOT NULL,
//                maxMineSize INT NOT NULL,
//                materialChance VARCHAR(50) NOT NULL
//                );""");
//
//        sqlHelper.executeUpdate("""
//                CREATE TABLE IF NOT EXISTS pregenmines (
//                location VARCHAR(20),
//                min_mining VARCHAR(20),
//                max_mining VARCHAR(20),
//                spawn VARCHAR(20),
//                min_full VARCHAR(20),
//                max_full VARCHAR(20)
//                );
//                """);
//        sqlHelper.setAutoCommit(true);
//
//        String databaseName = "privatemines";
//        List<String> cacheNames = List.of("owner", "mineType", "mineLocation", "corner1", "corner2");
//
//        cacheNames.forEach(string -> {
//            SQLCache sqlCache = sqlHelper.createCache(databaseName, string);
//            caches.put(string, sqlCache);
//        });
//
//        Task.asyncDelayed(this::loadSQLMines);
//        Task.asyncDelayed(SQLUtils::loadPregens);
//        Task.asyncDelayed(this::loadAddons);
//
//        Metrics metrics = new Metrics(this, PLUGIN_ID);
//        metrics.addCustomChart(new SingleLineChart("mines", () -> mineService.getMinesCount()));

        new UpdateChecker(this).fetch();
        minesAPI = new PrivateMinesAPIImpl(mineService);

        configurationProcessor.refreshFields();
    }

    private void registerMineTypes() {
        ConfigurationSection configurationSection = getConfig().getConfigurationSection("mine-types");
        for (String key : configurationSection.getKeys(false)) {
            MineType mineType = MineType.fromConfigurationSection(configurationSection.getConfigurationSection(key));
            mineTypeRegistry.register(mineType);
        }

    }

    private void esnureDirectoriesCreation() {
        schematicsDirectory = new File(getDataFolder(), "schematics");
        addonsDirectory = new File(getDataFolder(), "addons");
        if (!schematicsDirectory.exists()) schematicsDirectory.mkdirs();
        if (!addonsDirectory.exists()) schematicsDirectory.mkdirs();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configurationProcessor != null) configurationProcessor.refreshFields();
    }

    @Override
    public void onDisable() {
        LoggerUtil.info("PrivateMines is disabling..");
        saveMines();
    }

//    private void ensureDatabaseFileCreation() {
//        File databaseFile = new File(instance.getDataFolder(), "privatemines.db");
//        if (!databaseFile.exists()) {
//            databaseFile.getParentFile().mkdirs();
//            try {
//                databaseFile.createNewFile();
//            } catch (IOException e) {
//                logError("There was an error while trying to save the database file to the plugin's datafolder", e);
//            }
//        }
//    }

//    public void loadSQLMines() {
//        SQLHelper sqlHelper = getSqlHelper();
//        Results results = sqlHelper.queryResults("SELECT * FROM privatemines;");
//
//        results.forEach(result -> {
//            String owner = result.getString(1);
//            String mineType = result.getString(2);
//            String mineLocation = result.getString(3);
//            String mineAreaCornerMinString = result.getString(4);
//            String mineAreaCornerMaxString = result.getString(5);
//            String schematicAreaCornerMinString = result.getString(6);
//            String schematicAreaCornerMaxString = result.getString(7);
//            String spawn = result.getString(8);
//            double tax = result.get(9);
//            int isOpen = result.get(10);
//
////      String resultsMaterial = result.getString(13);
////      resultsMaterial = resultsMaterial.substring(1); // remove starting '{'
////
////      Map<Material, Double> materialChance = new HashMap<>();
////
////      String[] pairs = resultsMaterial.split("\\s*,\\s*");
////
////      for (String string : pairs) {
////        String[] parts = string.split("=");
////        String matString = parts[0];
////        double percent = Double.parseDouble(parts[1].substring(0, parts[1].length() - 1));
////        Material material = Material.valueOf(matString);
////        materialChance.put(material, percent);
////      }
//            UUID uuid = UUID.fromString(owner);
//            MineType type = mineTypeRegistry.get(mineType);
//
//            Location mineAreaCornerMin = LocationUtils.fromString(mineAreaCornerMinString);
//            Location mineAreaCornerMax = LocationUtils.fromString(mineAreaCornerMaxString);
//            Location schematicAreaCornerMin = LocationUtils.fromString(schematicAreaCornerMinString);
//            Location schematicAreaCornerMax = LocationUtils.fromString(schematicAreaCornerMaxString);
//
//            Location location = LocationUtils.fromString(mineLocation);
//            Location spawnLocation = LocationUtils.fromString(spawn);
//            boolean open = isOpen != 0;
//
//            MineStructure mineStructure = new MineStructure(
//                    mineAreaCornerMin,
//                    mineAreaCornerMax,
//                    schematicAreaCornerMin,
//                    schematicAreaCornerMax,
//                    location,
//                    spawnLocation
//            );
//
//            MineData mineData = new MineData(uuid, mineStructure, type);
//            mineData.setOpen(open);
//            mineData.setTax(tax);
//
//            Mine mine = new Mine(mineData);
//            mineService.cache(mine);
//        });
//    }

//    public void loadAddons() {
//        final PathMatcher jarMatcher = FileSystems.getDefault()
//                .getPathMatcher("glob:**/*.jar"); // Credits to Brister Mitten
//        PrivateMinesAPIImpl privateMinesAPIImpl = new PrivateMinesAPIImpl();
//
//        try (Stream<Path> paths = Files.walk(addonsDirectory.toPath()).filter(jarMatcher::matches)) {
//            paths.forEach(jar -> {
//                File file = jar.toFile();
//                privateMinesAPIImpl.loadAddon(file);
//            });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    public PrivateMinesAPI getMinesAPI() {
        return minesAPI;
    }

    public void saveMines() {
        for (Mine mine : mineService.getMines().values()) {
            Player player = Bukkit.getOfflinePlayer(mine.getMineData().getMineOwner()).getPlayer();
            if (player == null) return;

            // todo: update mines
//            SQLUtils.update(mine);
        }
    }

    public SchematicStorage getSchematicStorage() {
        return schematicStorage;
    }

    public MineFactory getMineFactory() {
        return mineFactory;
    }

    public MineService getMineService() {
        return mineService;
    }

    public MineTypeRegistry getMineTypeRegistry() {
        return mineTypeRegistry;
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

    public File getSchematicsDirectory() {
        return schematicsDirectory;
    }

    public AddonManager getAddonManager() {
        return addonManager;
    }
}
