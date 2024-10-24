/**
 * MIT License
 * <p>
 * Copyright (c) 2021 - 2023 Kyle Hicks
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.untouchedodin0.privatemines;

import me.untouchedodin0.kotlin.mine.storage.PregenStorage;
import me.untouchedodin0.privatemines.commands.AddonsCommand;
import me.untouchedodin0.privatemines.commands.PrivateMinesCommand;
import me.untouchedodin0.privatemines.commands.PublicMinesCommand;
import me.untouchedodin0.privatemines.configuration.ConfigurationProcessor;
import me.untouchedodin0.privatemines.factory.MineFactory;
import me.untouchedodin0.privatemines.iterator.SchematicIterator;
import me.untouchedodin0.privatemines.listener.MineResetListener;
import me.untouchedodin0.privatemines.listener.PlayerJoinListener;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.storage.SchematicStorage;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.storage.sql.SQLite;
import me.untouchedodin0.privatemines.utils.QueueUtils;
import me.untouchedodin0.privatemines.utils.UpdateChecker;
import me.untouchedodin0.privatemines.utils.addon.AddonManager;
import me.untouchedodin0.privatemines.utils.hook.WorldguardHook.WorldGuardRegionOrchestrator;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.misc.LocationUtils;
import redempt.redlib.misc.Task;
import redempt.redlib.sql.SQLCache;
import redempt.redlib.sql.SQLHelper;
import redempt.redlib.sql.SQLHelper.Results;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static me.untouchedodin0.privatemines.utils.hook.HookHandler.handleHooks;

public class PrivateMines extends JavaPlugin {
    private static PrivateMines instance;
    private static final int PLUGIN_ID = 11413;

    private File schematicsDirectory;
    private File addonsDirectory;

    private final Map<String, SQLCache> caches = new HashMap<>();

    private ConfigurationProcessor configurationProcessor;
    private SchematicStorage schematicStorage;
    private SchematicIterator schematicIterator;

    private MineFactory mineFactory;
    private MineService mineService;

    private PregenStorage pregenStorage;

    private MineWorldManager mineWorldManager;
    private MineTypeRegistry mineTypeRegistry;

    private QueueUtils queueUtils;

    private Economy economy = null;

    private SQLite sqLite;
    private SQLHelper sqlHelper;

    private AddonManager addonManager;
    private WorldGuardRegionOrchestrator regionOrchestrator;

    public static PrivateMines getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        logInfo("Loading Private Mines v" + getPluginMeta().getVersion());

        instance = this;
        configurationProcessor = new ConfigurationProcessor(this);
        saveDefaultConfig();

        schematicsDirectory = new File(getDataFolder(), "schematics");
        addonsDirectory = new File(getDataFolder(), "addons");
        if (!schematicsDirectory.exists()) schematicsDirectory.mkdirs();
        if (!addonsDirectory.exists()) schematicsDirectory.mkdirs();

        saveInitialResources();

        mineWorldManager = new MineWorldManager();
        mineFactory = new MineFactory();
        mineService = new MineService();
        pregenStorage = new PregenStorage();
        mineTypeRegistry = new MineTypeRegistry();
        queueUtils = new QueueUtils();
        addonManager = new AddonManager();

        schematicStorage = new SchematicStorage();
        schematicIterator = new SchematicIterator(schematicStorage);

        // TODO : Handle Tax System for mines.

        registerListeners();
        handleHooks();

        // todo: register mine types.

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

        AddonsCommand.registerCommands();
        PublicMinesCommand.registerCommands();
        PrivateMinesCommand.registerCommands();
        ensureDatabaseFileCreation();


        // TODO: rewrite whole sql data handle system.
        sqLite = new SQLite();
        sqLite.load();
        sqlHelper = new SQLHelper(sqLite.getSQLConnection());

        sqlHelper.executeUpdate("""
                CREATE TABLE IF NOT EXISTS privatemines (
                owner VARCHAR(36) UNIQUE NOT NULL,
                mineType VARCHAR(10) NOT NULL,
                mineLocation VARCHAR(30) NOT NULL,
                corner1 VARCHAR(30) NOT NULL,
                corner2 VARCHAR(30) NOT NULL,
                fullRegionMin VARCHAR(30) NOT NULL,
                fullRegionMax VARCHAR(30) NOT NULL,
                spawn VARCHAR(30) NOT NULL,
                tax FLOAT NOT NULL,
                isOpen INT NOT NULL,
                maxPlayers INT NOT NULL,
                maxMineSize INT NOT NULL,
                materials VARCHAR(50) NOT NULL
                );""");

        sqlHelper.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pregenmines (
                location VARCHAR(20),
                min_mining VARCHAR(20),
                max_mining VARCHAR(20),
                spawn VARCHAR(20),
                min_full VARCHAR(20),
                max_full VARCHAR(20)
                );
                """);
        sqlHelper.setAutoCommit(true);

        String databaseName = "privatemines";
        List<String> cacheNames = List.of("owner", "mineType", "mineLocation", "corner1", "corner2");

        cacheNames.forEach(string -> {
            SQLCache sqlCache = sqlHelper.createCache(databaseName, string);
            caches.put(string, sqlCache);
        });

        Task.asyncDelayed(this::loadSQLMines);
        Task.asyncDelayed(SQLUtils::loadPregens);
        Task.asyncDelayed(this::loadAddons);

        Metrics metrics = new Metrics(this, PLUGIN_ID);
        metrics.addCustomChart(new SingleLineChart("mines", () -> mineService.getMinesCount()));

        new UpdateChecker(this).fetch();
        configurationProcessor.refreshFields();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configurationProcessor != null) configurationProcessor.refreshFields();
    }

    @Override
    public void onDisable() {
        logInfo("PrivateMines is disabling..");
        saveMines();
        sqlHelper.close();
    }

    private void saveInitialResources() {
        List<String> locales = List.of("en", "es");
        for (String localeCodeName : locales) saveResource(String.format("messages_%s.yml", localeCodeName), false);
        saveResource("menus.yml", false);
    }

    private void ensureDatabaseFileCreation() {
        File databaseFile = new File(instance.getDataFolder(), "privatemines.db");
        if (!databaseFile.exists()) {
            databaseFile.getParentFile().mkdirs();
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                logError("There was an error while trying to save the database file to the plugin's datafolder", e);
            }
        }
    }

    public void logError(Exception exception) {
        logError("An error occurred: ", exception);
    }

    public void logError(String message, Exception ex) {
        getLogger().severe(message + ex.getMessage());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new MineResetListener(), this);
    }

    public void loadSQLMines() {
        SQLHelper sqlHelper = getSqlHelper();
        Results results = sqlHelper.queryResults("SELECT * FROM privatemines;");

        results.forEach(result -> {
            String owner = result.getString(1);
            String mineType = result.getString(2);
            String mineLocation = result.getString(3);
            String mineAreaCornerMinString = result.getString(4);
            String mineAreaCornerMaxString = result.getString(5);
            String schematicAreaCornerMinString = result.getString(6);
            String schematicAreaCornerMaxString = result.getString(7);
            String spawn = result.getString(8);
            double tax = result.get(9);
            int isOpen = result.get(10);

//      String resultsMaterial = result.getString(13);
//      resultsMaterial = resultsMaterial.substring(1); // remove starting '{'
//
//      Map<Material, Double> materials = new HashMap<>();
//
//      String[] pairs = resultsMaterial.split("\\s*,\\s*");
//
//      for (String string : pairs) {
//        String[] parts = string.split("=");
//        String matString = parts[0];
//        double percent = Double.parseDouble(parts[1].substring(0, parts[1].length() - 1));
//        Material material = Material.valueOf(matString);
//        materials.put(material, percent);
//      }
            UUID uuid = UUID.fromString(owner);
            MineType type = mineTypeRegistry.get(mineType);

            Location mineAreaCornerMin = LocationUtils.fromString(mineAreaCornerMinString);
            Location mineAreaCornerMax = LocationUtils.fromString(mineAreaCornerMaxString);
            Location schematicAreaCornerMin = LocationUtils.fromString(schematicAreaCornerMinString);
            Location schematicAreaCornerMax = LocationUtils.fromString(schematicAreaCornerMaxString);

            Location location = LocationUtils.fromString(mineLocation);
            Location spawnLocation = LocationUtils.fromString(spawn);
            boolean open = isOpen != 0;

            MineStructure mineStructure = new MineStructure(
                    mineAreaCornerMin,
                    mineAreaCornerMax,
                    schematicAreaCornerMin,
                    schematicAreaCornerMax,
                    location,
                    spawnLocation
            );

            MineData mineData = new MineData(uuid, mineStructure, type);
            mineData.setOpen(open);
            mineData.setTax(tax);

            Mine mine = new Mine(mineData);
            mineService.cache(mine);
        });
    }

    public void loadAddons() {
        final PathMatcher jarMatcher = FileSystems.getDefault()
                .getPathMatcher("glob:**/*.jar"); // Credits to Brister Mitten
        PrivateMinesAPI privateMinesAPI = new PrivateMinesAPI();

        try (Stream<Path> paths = Files.walk(addonsDirectory.toPath()).filter(jarMatcher::matches)) {
            paths.forEach(jar -> {
                File file = jar.toFile();
                privateMinesAPI.loadAddon(file);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveMines() {
        for (Mine mine : mineService.getMines().values()) {
            Player player = Bukkit.getOfflinePlayer(mine.getMineData().getMineOwner()).getPlayer();
            if (player == null) return;

            SQLUtils.update(mine);
            mine.stopTasks();
        }
    }

    public SchematicStorage getSchematicStorage() {
        return schematicStorage;
    }

    public MineFactory getMineFactory() {
        return mineFactory;
    }

    public PregenStorage getPregenStorage() {
        return pregenStorage;
    }

    public MineService getMineService() {
        return mineService;
    }

    public MineWorldManager getMineWorldManager() {
        return mineWorldManager;
    }

    public MineTypeRegistry getMineTypeManager() {
        return mineTypeRegistry;
    }

    public AddonManager getAddonManager() {
        return addonManager;
    }

    public void setEconomy(Economy economy) {
        this.economy = economy;
    }

    public Economy getEconomy() {
        return economy;
    }

    public SQLHelper getSqlHelper() {
        return sqlHelper;
    }

    public QueueUtils getQueueUtils() {
        return queueUtils;
    }

    public void logInfo(String message) {
        getLogger().info(message);
    }

    public File getAddonsDirectory() {
        return addonsDirectory;
    }

    public File getSchematicsDirectory() {
        return schematicsDirectory;
    }

    public void setRegionOrchestrator(WorldGuardRegionOrchestrator regionOrchestrator) {
        this.regionOrchestrator = regionOrchestrator;
    }

    public WorldGuardRegionOrchestrator getRegionOrchestrator() {
        return regionOrchestrator;
    }
}
