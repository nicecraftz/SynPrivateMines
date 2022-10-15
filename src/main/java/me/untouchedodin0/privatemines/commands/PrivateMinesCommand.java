package me.untouchedodin0.privatemines.commands;

import me.untouchedodin0.kotlin.menu.Menu;
import me.untouchedodin0.kotlin.mine.data.MineData;
import me.untouchedodin0.kotlin.mine.pregen.PregenMine;
import me.untouchedodin0.kotlin.mine.storage.MineStorage;
import me.untouchedodin0.kotlin.mine.storage.PregenStorage;
import me.untouchedodin0.kotlin.mine.type.MineType;
import me.untouchedodin0.kotlin.utils.AudienceUtils;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.WorldBorderUtils;
import me.untouchedodin0.privatemines.config.MenuConfig;
import me.untouchedodin0.privatemines.factory.MineFactory;
import me.untouchedodin0.privatemines.factory.PregenFactory;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineTypeManager;
import me.untouchedodin0.privatemines.playershops.Shop;
import me.untouchedodin0.privatemines.playershops.ShopBuilder;
import me.untouchedodin0.privatemines.utils.MessageUtils;
import me.untouchedodin0.privatemines.utils.inventory.PublicMinesMenu;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redempt.redlib.RedLib;
import redempt.redlib.commandmanager.CommandHook;
import redempt.redlib.commandmanager.Messages;
import redempt.redlib.misc.ChatPrompt;
import redempt.redlib.misc.Task;
import redempt.redlib.sql.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class PrivateMinesCommand {

    PrivateMines privateMines = PrivateMines.getPrivateMines();
    MineStorage mineStorage = privateMines.getMineStorage();
    MineTypeManager mineTypeManager = privateMines.getMineTypeManager();

    @CommandHook("main")
    public void main(CommandSender sender) {
        if (sender instanceof Player player) {

            AudienceUtils audienceUtils = new AudienceUtils();
            audienceUtils.sendMessage(player, "test");
            audienceUtils.sendMessage(player, "Hello <rainbow>world</rainbow>, isn't <blue><u><click:open_url:'https://docs.adventure.kyori.net/minimessage'>MiniMessage</click></u></blue> fun?");
//            MessageUtils messageUtils = new MessageUtils();
//            TextComponent textComponent = Component.text("Test text command!");

            if (!(player.getUniqueId() == UUID.fromString("79e6296e-6dfb-4b13-9b27-e1b37715ce3b"))) {
                Menu mainMenu = MenuConfig.getMenus().get("mainMenu");
                mainMenu.open(player);
            } else {
                if (RedLib.MID_VERSION < 19) {
                    Menu mainMenu = MenuConfig.getMenus().get("mainMenu");
                    mainMenu.open(player);
                } else {
                    WorldBorderUtils worldBorderUtils = new WorldBorderUtils();
                    Server server = Bukkit.getServer();
                    Location location = player.getLocation();
                    double size = 5;

                    player.sendMessage("worldBorderUtils: " + worldBorderUtils);

                    worldBorderUtils.clearBorder(player);
                    if (worldBorderUtils.isSetBorder()) {
                        worldBorderUtils.clearBorder(player);
                    } else {
                        worldBorderUtils.sendWorldBorder(server, player, location, size);
                    }
                }
            }
        }
    }

    @CommandHook("give")
    public void give(CommandSender commandSender, OfflinePlayer target, MineType mineType) {
        MineFactory mineFactory = new MineFactory();
        MineWorldManager mineWorldManager = privateMines.getMineWorldManager();
        Location location = mineWorldManager.getNextFreeLocation();
        MineType defaultMineType = mineTypeManager.getDefaultMineType();

        if (target.getPlayer() != null) {
            if (mineStorage.hasMine(target.getUniqueId())) {
                commandSender.sendMessage(Messages.msg("playerAlreadyOwnsAMine"));
            } else {
                mineFactory.create(target.getPlayer(), location, Objects.requireNonNullElse(mineType, defaultMineType));
                commandSender.sendMessage(ChatColor.GREEN + "You gave " + target.getName() + " a private mine!");
            }
        }
    }

    @CommandHook("delete")
    public void delete(CommandSender commandSender, OfflinePlayer target) {
        if (!mineStorage.hasMine(target.getUniqueId())) {
            commandSender.sendMessage(ChatColor.RED + "Player doesn't own a mine!");
        } else {
            Mine mine = mineStorage.get(target.getUniqueId());
            commandSender.sendMessage(ChatColor.GREEN + "You deleted " + target.getName() + "'s private mine!");
            if (mine != null) {
                mine.delete();
            }
        }
    }

    @CommandHook("reset")
    public void reset(Player player) {

        if (!mineStorage.hasMine(player)) {
            player.sendMessage(Messages.msg("youDontOwnAMine"));
        } else {
            Mine mine = mineStorage.get(player);
            if (mine != null) {
                mine.reset();
            }
            player.sendMessage(Messages.msg("yourMineHasBeenReset"));
        }
    }

    @CommandHook("upgrade")
    public void upgrade(CommandSender commandSender, Player player) {
        if (!mineStorage.hasMine(player)) {
            commandSender.sendMessage(ChatColor.RED + "That player doesn't own a mine!");
            player.sendMessage(Messages.msg("youDontOwnAMine"));
        } else {
            Mine mine = mineStorage.get(player);
            if (mine != null) {
                mine.upgrade();
            }
        }
    }

    @CommandHook("expand")
    public void expand(CommandSender commandSender, Player target, int amount) {
        if (!mineStorage.hasMine(target)) {
            commandSender.sendMessage(ChatColor.RED + "That player doesn't own a mine!");
        } else {
            Mine mine = mineStorage.get(target);
            if (mine != null) {
                if (mine.canExpand(amount)) {
                    for (int i = 0; i < amount; i++) {
                        mine.expand();
                    }
                }
                MineData mineData = mine.getMineData();
                mine.saveMineData(target, mineData);
            }
        }
    }

    @CommandHook("teleport")
    public void teleport(Player player) {
        if (!mineStorage.hasMine(player.getUniqueId())) {
            player.sendMessage(Messages.msg("youDontOwnAMine"));
        } else {
            Mine mine = mineStorage.get(player.getUniqueId());
            player.sendMessage(Messages.msg("youHaveBeenTeleportedToYourMine"));
            if (mine != null) {
                mine.teleport(player);
            }
        }
    }

    @CommandHook("visit")
    public void visit(Player player, OfflinePlayer target) {
        if (!mineStorage.hasMine(target.getUniqueId())) {
            player.sendMessage(Messages.msg("playerDoesntOwnAMine"));
        } else {
            Mine mine = mineStorage.get(target.getUniqueId());
            if (mine != null) {
                mine.teleport(player);
                player.sendMessage(ChatColor.GREEN + "You are now visiting " + target.getName() + "'s mine!");
            }
        }
    }

    @CommandHook("setblocks")
    public void setBlocks(CommandSender commandSender, Player target, Material[] materials) {
        Mine mine = mineStorage.get(target);
        MineData mineData;
        Map<Material, Double> map = new HashMap<>();

        if (mine != null) {
            for (Material material : materials) {
                if (material.isBlock()) {
                    map.put(material, 1.0);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Could not add " + material.name() + " as it wasn't a solid block!");
                }
            }

            mineData = mine.getMineData();
            mineData.setMaterials(map);
            mine.setMineData(mineData);
            mine.saveMineData(target, mineData);
            mineStorage.replaceMine(target.getUniqueId(), mine);
            mine.reset();
        }
    }

    @CommandHook("tax")
    public void tax(Player player, double tax) {
        Mine mine = mineStorage.get(player);
        MineData mineData;
        if (mine != null) {
            mineData = mine.getMineData();
            mineData.setTax(tax);
            mine.setMineData(mineData);
            mine.saveMineData(player, mineData);
            player.sendMessage(ChatColor.GREEN + "Successfully set your tax to " + tax + "%");
        }
    }

    @CommandHook("ban")
    public void ban(Player player, Player target) {
        Mine mine = mineStorage.get(player);
        if (mine != null) {
            MineData mineData = mine.getMineData();
            UUID uuid = target.getUniqueId();

            if (mineData.getBannedPlayers().contains(uuid)) {
                player.sendMessage(ChatColor.RED + "Target is already banned!");
            } else {
                mineData.getBannedPlayers().add(uuid);
                mine.setMineData(mineData);
                mine.saveMineData(player, mineData);
                player.sendMessage(ChatColor.GREEN + "Successfully banned " + target.getName());
            }
        }
    }

    @CommandHook("unban")
    public void unban(Player player, Player target) {
        Mine mine = mineStorage.get(player);
        if (mine != null) {
            MineData mineData = mine.getMineData();
            UUID uuid = target.getUniqueId();

            if (!mineData.getBannedPlayers().contains(uuid)) {
                player.sendMessage(ChatColor.RED + "Target isn't banned!");
            } else {
                mineData.getBannedPlayers().remove(uuid);
                mine.setMineData(mineData);
                mine.saveMineData(player, mineData);
                player.sendMessage(ChatColor.GREEN + "Successfully unbanned " + target.getName());
            }
        }
    }

    @CommandHook("open")
    public void open(Player player) {
        Mine mine = mineStorage.get(player);

        if (mine != null) {
            MineData mineData = mine.getMineData();
            mineData.setOpen(true);
            mine.saveMineData(player, mineData);
            player.sendMessage(ChatColor.GREEN + "Your mine's now open!");
        }
    }

    @CommandHook("close")
    public void close(Player player) {
        Mine mine = mineStorage.get(player);

        if (mine != null) {
            MineData mineData = mine.getMineData();
            mineData.setOpen(false);
            mine.saveMineData(player, mineData);
            player.sendMessage(ChatColor.RED + "Your mine's now closed!");
        }
    }

    @CommandHook("debug")
    public void debug(Player player) {
        PublicMinesMenu menu = new PublicMinesMenu();
        menu.open(player);
        player.sendMessage("debug.");
    }

    @CommandHook("pregen")
    public void pregen(Player player, int amount) {
        PregenFactory pregenFactory = new PregenFactory();
        pregenFactory.generate(player, amount);
    }

    @CommandHook("claim")
    public void claim(Player player) {

        if (mineStorage.hasMine(player)) {
            player.sendMessage(ChatColor.RED + "You already own a mine!");
            return;
        }
        PregenStorage pregenStorage = privateMines.getPregenStorage();
        boolean isAllRedeemed = pregenStorage.isAllRedeemed();

        if (isAllRedeemed) {
            player.sendMessage(ChatColor.RED + "All the mines have been redeemed, please contact an");
            player.sendMessage(ChatColor.RED + "admin and ask them to redeem some more mines!");
        } else {
            PregenMine pregenMine = pregenStorage.getAndRemove();

            if (pregenMine != null) {
                Location location = pregenMine.getLocation();
                Location spawnLocation = pregenMine.getSpawnLocation();
                Location lowerRails = pregenMine.getLowerRails();
                Location upperRails = pregenMine.getUpperRails();
                Location fullMin = pregenMine.getFullMin();
                Location fullMax = pregenMine.getFullMax();

                UUID uuid = player.getUniqueId();

                if (spawnLocation != null) {
                    spawnLocation.getBlock().setType(Material.AIR);
                }

                MineType mineType = mineTypeManager.getDefaultMineType();

                Map<Material, Double> prices = new HashMap<>();
                Map<Material, Double> materials = mineType.getMaterials();
                if (materials != null) {
                    prices.putAll(materials);
                }
                Shop shop = new ShopBuilder().setOwner(uuid).setPrices(prices).build();

                Mine mine = new Mine(privateMines);
                assert upperRails != null;
                assert lowerRails != null;
                assert fullMin != null;
                assert fullMax != null;
                assert location != null;
                assert spawnLocation != null;

                MineData mineData = new MineData(
                        uuid,
                        upperRails,
                        lowerRails,
                        fullMin,
                        fullMax,
                        location,
                        spawnLocation,
                        mineType,
                        shop
                );

                mine.setMineData(mineData);
                mine.saveMineData(player, mineData);
                mineStorage.addMine(player.getUniqueId(), mine);
                mine.reset();

                Task.syncDelayed(() -> pregenMine.teleport(player), 5L);
            }
        }
    }

    @CommandHook("convert")
    public void convert(Player player) {
        ChatPrompt.prompt(player, ChatColor.YELLOW + "Are you sure you want to convert the mines to SQL? Type Yes to carry on the process " +
                "or No to cancel the process.", str -> {
            if (str.equalsIgnoreCase("Yes")) {
                player.sendMessage(ChatColor.GREEN + "Starting conversion...");
                SQLHelper sqlHelper = privateMines.getSqlHelper();
                Connection connection = sqlHelper.getConnection();
                String query = "INSERT INTO privatemines values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                // 13

                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, "1");
                    preparedStatement.setString(2, "2");
                    preparedStatement.setString(3, "3");
                    preparedStatement.setString(4, "4");
                    preparedStatement.setString(5, "5");
                    preparedStatement.setString(6, "6");
                    preparedStatement.setString(7, "7");
                    preparedStatement.setString(8, "8");
                    preparedStatement.setString(9, "9");
                    preparedStatement.setString(10, "10");
                    preparedStatement.setString(11, "11");
                    preparedStatement.setString(12, "12");
                    preparedStatement.setString(13, "13");

                    int res = preparedStatement.executeUpdate();

//                    connection.commit();
//                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage("connection " + connection);

                try {
                    connection.commit();
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }



//                sqlHelper.executeUpdate("UPDATE privatemines SET mineOwner=? mineType=? mineLocation=? corner1=? corner2=? fullRegionMin=? fullRegionMax=? spawn=? tax=? isOpen=? maxPlayers=? maxMineSize=? materials=?", "owner", "type", "minelocation", "corner1", "corner2", "fullmin", "fullmax", "spawn", 5.0, false, 1, 1, "materials");
//                sqlHelper.commit();
//
//                player.sendMessage("" + sqlHelper);
//                sqlHelper.execute("INSERT INTO privatemines (mineOwner, mineType, mineLocation, corner1, corner2, fullRegionMin, fullRegionMax, spawn, tax, isOpen, maxPlayers, maxMineSize, materials) VALUES(mineOwner, mineType, mineLocation, corner1, corner2, fullRegionMin, fullRegionMax, spawn, tax, isOpen, maxPlayers, maxMineSize, materials);");

//                privateMines.convertToSQL(player);
            } else {
                player.sendMessage(ChatColor.RED + "Cancelled the process.");
            }
        });
    }

    @CommandHook("reload")
    public void reload(Player player) {
        privateMines.getConfigManager().reload();
        privateMines.getConfigManager().load();
    }

    @CommandHook("setborder")
    public void setBorder(Player player, Player target, int size) {
        WorldBorderUtils worldBorderUtils = privateMines.getWorldBorderUtils();
        Server server = Bukkit.getServer();
        Location location = player.getLocation();

        player.sendMessage("worldBorderUtils: " + worldBorderUtils);
        worldBorderUtils.sendWorldBorder(server, player, location, size);
    }

    @CommandHook("clearborder")
    public void clearborder(Player player, Player target) {
        WorldBorderUtils worldBorderUtils = privateMines.getWorldBorderUtils();
        WorldBorder worldBorder = worldBorderUtils.getWorldBorder(player);
        worldBorderUtils.clearBorder(player);
    }
}