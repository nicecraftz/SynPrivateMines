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
package me.untouchedodin0.privatemines.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.untouchedodin0.kotlin.mine.pregen.PregenMine;
import me.untouchedodin0.kotlin.mine.storage.PregenStorage;
import me.untouchedodin0.kotlin.utils.Range;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.factory.MineFactory;
import me.untouchedodin0.privatemines.factory.PregenFactory;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.storage.sql.SQLUtils;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.*;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


//@CommandAlias("privatemine|privatemines|pmine")
public class PrivateMinesCommand {
    private static final PrivateMines PLUGIN_INSTANCE = PrivateMines.getInstance();
    private static final Economy ECONOMY = PLUGIN_INSTANCE.getEconomy();
    //    private static final SQLHelper SQL_HELPER = PLUGIN_INSTANCE.getSqlHelper();
    private static final MineService MINE_SERVICE = PLUGIN_INSTANCE.getMineService();
    private static final MineTypeRegistry MINE_TYPE_MANAGER = PLUGIN_INSTANCE.getMineTypeRegistry();

    private PrivateMinesCommand() {

    }

    public static void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = PLUGIN_INSTANCE.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private static LiteralCommandNode<CommandSourceStack> buildCommand() {

        return literal("privatemine").then(versionCommandLogic())
                .then(giveCommandLogic())
                .then(deleteMineLogic())
                .then(upgradeMineLogic())
                .then(forceUpgradeMineLogic())
                .build();
    }

    private static CommandNode<CommandSourceStack> versionCommandLogic() {
        return literal("version").executes(context -> {
            String localVersion = PLUGIN_INSTANCE.getDescription().getVersion();
            context.getSource().getSender().sendRichMessage("<green>Private Mines is running v" + localVersion);
            return SINGLE_SUCCESS;
        }).build();
    }

    private static CommandNode<CommandSourceStack> giveCommandLogic() {
        return literal("give").requires(commandSourceStack -> commandSourceStack.getSender()
                        .hasPermission("privatemines.give"))
                .then(argument("target", ArgumentTypes.player()).executes(context -> {
                    CommandSender commandSender = context.getSource().getSender();
                    Player target = context.getArgument("target", Player.class);

                    if (target == null || !target.isOnline()) {
                        commandSender.sendRichMessage("<red>The specified player couldn't be found!");
                        return SINGLE_SUCCESS;
                    }

                    MineFactory mineFactory = new MineFactory();
                    MineWorldManager mineWorldManager = PLUGIN_INSTANCE.getMineWorldManager();
                    Location location = mineWorldManager.getNextFreeLocation();
                    MineType defaultMineType = MINE_TYPE_MANAGER.getDefault();


                    if (MINE_SERVICE.has(target.getUniqueId())) {
                        commandSender.sendRichMessage("<red>The specified player already owns a mine!");
                        return SINGLE_SUCCESS;
                    }

                    mineFactory.create(target.getPlayer(), location, defaultMineType);
                    commandSender.sendRichMessage("<green>Gave " + target.getName() + " a mine!");
                    return SINGLE_SUCCESS;
                }))
                .build();
    }

    private static CommandNode<CommandSourceStack> deleteMineLogic() {
        return literal("delete").requires(commandSourceStack -> commandSourceStack.getSender()
                        .hasPermission("privatemines.delete"))
                .then(argument("target", ArgumentTypes.player()).executes(context -> {
                    CommandSender commandSender = context.getSource().getSender();
                    Player target = context.getArgument("target", Player.class);

                    if (target == null || !target.isOnline()) {
                        commandSender.sendRichMessage("<red>The specified player couldn't be found!");
                        return SINGLE_SUCCESS;
                    }

                    if (!MINE_SERVICE.has(target.getUniqueId())) {
                        commandSender.sendRichMessage("<red>The specified player does not own a mine!");
                        return SINGLE_SUCCESS;
                    }

                    Mine mine = MINE_SERVICE.get(target.getUniqueId());
                    mine.stopTasks();
                    MINE_SERVICE.delete(mine);
                    SQLUtils.delete(mine);

                    commandSender.sendRichMessage("<green>Successfully deleted mine of player " + target.getName());
                    return SINGLE_SUCCESS;
                }))
                .build();
    }

    private static CommandNode<CommandSourceStack> upgradeMineLogic() {
        return literal("upgrade").requires(commandSourceStack -> {
            CommandSender sender = commandSourceStack.getSender();
            return sender.hasPermission("privatemines.upgrade") && sender instanceof Player;
        }).then(argument("times", IntegerArgumentType.integer(1)).executes(context -> {
            Player player = (Player) context.getSource().getSender();
            int times = context.getArgument("times", Integer.class);

            if (!MINE_SERVICE.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = MINE_SERVICE.get(player.getUniqueId());

            for (int i = 0; i < times; i++) {
                MineData mineData = mine.getMineData();

                MineType currentType = mineData.getMineType();
                MineType nextType = MINE_TYPE_MANAGER.getNextMineType(currentType);

                if (currentType == nextType) {
                    player.sendRichMessage("<green>Your mine is already at maxMineCorner level!");
                    return SINGLE_SUCCESS;
                }

                double cost = nextType.upgradeCost();
                if (!ECONOMY.has(player, cost)) {
                    player.sendRichMessage("<red>You cannot afford this upgrade!");
                    return SINGLE_SUCCESS;
                }

                ECONOMY.withdrawPlayer(player, cost);
                MINE_SERVICE.upgrade(mine);
                mine.handleReset();
                player.sendRichMessage("<green>Mine upgraded successfully!");
            }

            return SINGLE_SUCCESS;
        })).build();
    }

    private static CommandNode<CommandSourceStack> forceUpgradeMineLogic() {
        return literal("forceupgrade").requires(commandSourceStack -> {
            return commandSourceStack.getSender().hasPermission("privatemines.forceupgrade");
        }).then(argument("target", ArgumentTypes.player()).executes(context -> {
            CommandSender sender = context.getSource().getSender();
            Player target = context.getArgument("target", Player.class);
            if (!MINE_SERVICE.has(target.getUniqueId())) {
                sender.sendRichMessage("<red>This player does not have a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = MINE_SERVICE.get(target.getUniqueId());

            // todo: seems like this is used more than 2 times so its best to make a method.
            MINE_SERVICE.upgrade(mine);
            MINE_SERVICE.reset(mine);

            target.sendRichMessage("<green>Your mine has been upgraded!");

            MineData mineData = mine.getMineData();
            MineStructure mineStructure = mineData.getMineStructure();

            BoundingBox schematicArea = mineStructure.schematicBoundingBox();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (schematicArea.contains(onlinePlayer.getLocation().toVector()) && isInMineWorld(onlinePlayer)) {
                    Bukkit.dispatchCommand(onlinePlayer, "spawn"); //todo: change this since command may not exist.
                }
            }

            Task.asyncDelayed(() -> {
                SQL_HELPER.executeUpdate(
                        "INSERT INTO privatemines (owner, mineType, mineLocation, corner1, corner2, fullRegionMin, fullRegionMax, spawn, tax, isOpen, maxPlayers, maxMineSize, materials) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        String.valueOf(mineData.getMineOwner()),
                        mineData.getMineType(),
                        LocationUtils.toString(mineData.getMineLocation()),
                        LocationUtils.toString(mineData.getMinimumMining()),
                        LocationUtils.toString(mineData.getMaximumMining()),
                        LocationUtils.toString(mineData.getMinimumFullRegion()),
                        LocationUtils.toString(mineData.getMaximumFullRegion()),
                        LocationUtils.toString(mineData.getSpawnLocation()),
                        mineData.getTax(),
                        mineData.isOpen(),
                        mineData.getMaxPlayers(),
                        mineData.getMaxMineSize(),
                        mineData.getMaterials()
                );
            });

            return SINGLE_SUCCESS;
        })).build();
    }

    @Subcommand("forcereset")
    @CommandCompletion("@players")
    @CommandPermission("privatemines.forcereset")
    @Syntax("<target>")
    public void forceReset(CommandSender sender, OfflinePlayer target) {
        if (!MINE_SERVICE.hasMine(target.getUniqueId())) {
            if (sender instanceof Player player) {
                audienceUtils.sendMessage(player, MessagesConfig.playerDoesntOwnMine);
            }
        } else {
            Mine mine = MINE_SERVICE.get(target.getUniqueId());
            if (mine != null) mine.handleReset();
        }
    }


    @Subcommand("reset")
    @CommandPermission("privatemines.reset")
    public void reset(Player player) {
        if (!MINE_SERVICE.hasMine(player)) {
            audienceUtils.sendMessage(player, MessagesConfig.dontOwnMine);
        } else {
            int timeLeft = cooldowns.get(player.getUniqueId());
            Mine mine = MINE_SERVICE.get(player);

            if (!Config.enableResetCooldown) {
                if (mine != null) {
                    mine.handleReset();
                }
            } else {
                if (timeLeft == 0) {
                    if (mine != null) {
                        mine.handleReset();
                    }
                    cooldowns.set(player.getUniqueId(), Config.resetCooldown);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int timeLeft = cooldowns.get(player.getUniqueId());
                            cooldowns.set(player.getUniqueId(), --timeLeft);
                            if (timeLeft == 0) {
                                this.cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(PLUGIN_INSTANCE, 20, 20);
                } else {
                    //Hasn't expired yet, shows how many seconds left until it does
                    player.sendMessage(String.format(
                            ChatColor.RED + "Please wait %d seconds to reset your mine again!",
                            timeLeft
                    ));
                }
            }
        }
    }

    @Subcommand("teleport")
    @CommandPermission("privatemines.teleport")
    public void teleport(Player player) {
        if (!MINE_SERVICE.hasMine(player)) {
            audienceUtils.sendMessage(player, MessagesConfig.dontOwnMine);
        } else {
            Mine mine = MINE_SERVICE.get(player);
            if (mine != null) {
                mine.teleport(player);
            }
        }
    }

    @Subcommand("go")
    @CommandCompletion("@players")
    @CommandPermission("privatemines.go")
    @Syntax("<target>")
    public void go(Player player, OfflinePlayer target) {
        if (target.getPlayer() != null) {
            Player targetPlayer = target.getPlayer();
            Mine mine = MINE_SERVICE.get(targetPlayer);
            if (mine != null) {
                MineData mineData = mine.getMineData();
                if (mineData != null) {
                    List<UUID> banned = mineData.getBannedPlayers();

                    if (banned.contains(player.getUniqueId())) {
                        audienceUtils.sendMessage(player, MessagesConfig.bannedFromMine);
                    } else {
                        if (mineData.isOpen()) {
                            mine.teleport(player);
                            mine.startTasks();
                        } else {
                            audienceUtils.sendMessage(player, MessagesConfig.targetMineClosed);
                        }
                    }
                }
            }
        }
    }

    @Subcommand("expand")
    @CommandCompletion("@players")
    @CommandPermission("privatemines.expand")
    @Syntax("<target> <amount>")
    public void expand(CommandSender commandSender, OfflinePlayer target, int amount) {
        if (!MINE_SERVICE.hasMine(Objects.requireNonNull(target.getPlayer()))) {
            return;
        }
        Mine mine = MINE_SERVICE.get(target.getPlayer());
        if (mine != null) {
            if (mine.canExpand(amount)) {
                for (int i = 0; i < amount; i++) {
                    mine.expand();
                }
                commandSender.sendMessage(ChatColor.GREEN + "Successfully expanded " + target.getName() + "'s mine!");
            }
        }
    }

    @Subcommand("open")
    @CommandPermission("privatemines.open")
    public void open(Player player) {
        Mine mine = MINE_SERVICE.get(player);
        MineData mineData;
        if (mine != null) {
            mineData = mine.getMineData();
            mineData.setOpen(true);
            mine.setMineData(mineData);
            MINE_SERVICE.replaceMineNoLog(player, mine);
            SQLUtils.update(mine);
            audienceUtils.sendMessage(player, MessagesConfig.mineOpened);
        }
    }

    @Subcommand("close")
    @CommandPermission("privatemines.close")
    public void close(Player player) {
        Mine mine = MINE_SERVICE.get(player);
        MineData mineData;
        if (mine != null) {
            mineData = mine.getMineData();
            mineData.setOpen(false);
            mine.setMineData(mineData);
            MINE_SERVICE.replaceMineNoLog(player, mine);
            SQLUtils.update(mine);
            audienceUtils.sendMessage(player, MessagesConfig.mineClosed);
        }
    }

    @Subcommand("ban")
    @CommandPermission("privatemines.ban")
    @Syntax("<target>")
    public void ban(Player player, Player target) {
        Mine mine = MINE_SERVICE.get(player);
        if (mine != null) {
            mine.ban(target);
            MINE_SERVICE.replaceMineNoLog(player, mine);
            SQLUtils.update(mine);
            audienceUtils.sendMessage(player, target, MessagesConfig.successfullyBannedPlayer);
        }
    }

    @Subcommand("unban")
    @CommandPermission("privatemines.unban")
    @Syntax("<target>")
    public void unban(Player player, Player target) {
        Mine mine = MINE_SERVICE.get(player);
        if (mine != null) {
            mine.unban(target);
            MINE_SERVICE.replaceMineNoLog(player, mine);
            SQLUtils.update(mine);
            audienceUtils.sendMessage(player, target, MessagesConfig.unbannedPlayer);
        }
    }

    @Subcommand("tax")
    @CommandPermission("privatemines.tax")
    @Syntax("<amount>")
    public void tax(Player player, double tax) {
        Range range = new Range(0, 100);
        BukkitAudiences audiences = PLUGIN_INSTANCE.getAdventure();
        Audience audience = audiences.player(player);

        if (!range.contains(tax)) {
            audienceUtils.sendMessage(player, MessagesConfig.taxLimit);
        } else {
            Mine mine = MINE_SERVICE.get(player);
            if (mine != null) {
                MineData mineData = mine.getMineData();
                mineData.setTax(tax);
                mine.setMineData(mineData);
                MINE_SERVICE.replaceMineNoLog(player, mine);
                SQLUtils.update(mine);
                Component message = Component.text()
                        .append(Component.text("Successfully updated tax to ", NamedTextColor.GREEN))
                        .append(Component.text(String.format("%.2f%%", tax), NamedTextColor.GOLD))
                        .build();
                audienceUtils.sendMessage(player, MessagesConfig.setTax, tax);
            }
        }
    }

    @Subcommand("setblocks")
    @CommandCompletion("@players")
    @CommandPermission("privatemines.setblocks")
    @Syntax("<target> <materials> e.g. DIRT, STONE")
    public void setBlocks(CommandSender sender, OfflinePlayer target, @Split(",") String[] materials) {
        Map<Material, Double> map = new HashMap<>();

        for (String s : materials) {
            if (Material.getMaterial(s.toUpperCase()) == null) {
                sender.sendMessage(ChatColor.RED + "Failed to find Material: " + s);
                return;
            }
            Material material = Material.valueOf(s.toUpperCase());
            map.put(material, 1.0);
        }

        if (target != null) {
            Mine mine = MINE_SERVICE.get(Objects.requireNonNull(target.getPlayer()));
            if (mine != null) {
                MineData mineData = mine.getMineData();
                mineData.setMaterials(map);
                mine.setMineData(mineData);
                MINE_SERVICE.replaceMineNoLog(target.getPlayer(), mine);
                mine.handleReset();
                Task.asyncDelayed(() -> SQLUtils.update(mine));
            }
        }
    }

    @Subcommand("pregen")
    @CommandPermission("privatemines.pregen")
    @Syntax("<amount>")
    public void pregen(Player player, int amount) {
        PregenFactory.pregen(player, amount);
    }

    @Subcommand("claim")
    @CommandPermission("privatemines.claim")
    public void claim(Player player) {
        if (MINE_SERVICE.hasMine(player)) {
            audienceUtils.sendMessage(player, MessagesConfig.playerAlreadyOwnsAMine);
        } else {
            String mineRegionName = String.format("mine-%s", player.getUniqueId());
            String fullRegionName = String.format("full-mine-%s", player.getUniqueId());

            PregenStorage pregenStorage = PLUGIN_INSTANCE.getPregenStorage();
            if (pregenStorage.isAllRedeemed()) {
//        player.sendMessage(ChatColor.RED + "All the mines have been claimed...");
                audienceUtils.sendMessage(player, MessagesConfig.allMinesClaimed);
            } else {
                PregenMine pregenMine = pregenStorage.getAndRemove();
                MineType mineType = MINE_TYPE_MANAGER.getDefault();
                Location location = pregenMine.getLocation();
                Location spawn = pregenMine.getSpawnLocation();
                Location corner1 = pregenMine.getLowerRails();
                Location corner2 = pregenMine.getUpperRails();
                Location minimum = pregenMine.getFullMin();
                Location maximum = pregenMine.getFullMax();
                BlockVector3 miningRegionMin = BukkitAdapter.asBlockVector(Objects.requireNonNull(corner1));
                BlockVector3 miningRegionMax = BukkitAdapter.asBlockVector(Objects.requireNonNull(corner2));
                BlockVector3 fullRegionMin = BukkitAdapter.asBlockVector(Objects.requireNonNull(minimum));
                BlockVector3 fullRegionMax = BukkitAdapter.asBlockVector(Objects.requireNonNull(maximum));

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(spawn)
                        .getWorld()));

                ProtectedCuboidRegion miningRegion = new ProtectedCuboidRegion(
                        mineRegionName,
                        miningRegionMin,
                        miningRegionMax
                );
                ProtectedCuboidRegion fullRegion = new ProtectedCuboidRegion(
                        fullRegionName,
                        fullRegionMin,
                        fullRegionMax
                );

                if (regionManager != null) {
                    regionManager.addRegion(miningRegion);
                    regionManager.addRegion(fullRegion);
                }

                Mine mine = new Mine(PLUGIN_INSTANCE);
                MineData mineData = new MineData(
                        player.getUniqueId(),
                        corner2,
                        corner1,
                        minimum,
                        maximum,
                        Objects.requireNonNull(location),
                        spawn,
                        mineType,
                        false,
                        5.0
                );
                mine.setMineData(mineData);
                SQLUtils.claim(location);
                SQLUtils.insert(mine);

                MINE_SERVICE.addMine(player.getUniqueId(), mine);

                Task.syncDelayed(() -> spawn.getBlock().setType(Material.AIR, false));
                pregenMine.teleport(player);
                mine.handleReset();
            }
        }
    }
}
