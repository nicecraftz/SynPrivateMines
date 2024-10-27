package me.untouchedodin0.privatemines.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.configuration.ConfigurationEntry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.factory.MineFactory;
import me.untouchedodin0.privatemines.mine.*;
import me.untouchedodin0.privatemines.utils.Cooldowns;
import me.untouchedodin0.privatemines.utils.world.MineWorldManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


public class PrivateMinesCommand {
    private final PrivateMines privateMines;
    private final Economy economy;
    private final MineService mineService;
    private final MineTypeRegistry mineTypeRegistry;
    private final MineWorldManager mineWorldManager;
    private final MineFactory mineFactory;

    @ConfigurationEntry(key = "reset-cooldown", section = "mine", value = "15", type = ConfigurationValueType.INT)
    private int resetCooldown;

    public PrivateMinesCommand(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.economy = privateMines.getEconomy();
        this.mineService = privateMines.getMineService();
        this.mineTypeRegistry = privateMines.getMineTypeRegistry();
        this.mineWorldManager = privateMines.getMineWorldManager();
        this.mineFactory = privateMines.getMineFactory();

        ConfigurationInstanceRegistry.registerInstance(this);
    }

    public void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = privateMines.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private LiteralCommandNode<CommandSourceStack> buildCommand() {

        return literal("privatemine").then(versionLogic())
                .then(giveLogic())
                .then(deleteMineLogic())
                .then(upgradeMineLogic())
                .then(forceUpgradeMineLogic())
                .then(forceResetMineLogic())
                .then(resetMineLogic())
                .then(teleportMineLogic())
                .then(goMineLogic())
                .then(expandMineLogic())
                .then(openCloseMineLogic("open", true))
                .then(openCloseMineLogic("close", false))
                .then(banFromMineLogic())
                .then(unbanFromMineLogic())
                .then(taxLogic())
                .then(setBlocksLogic())
                .build();
    }

    private CommandNode<CommandSourceStack> versionLogic() {
        return literal("version").executes(context -> {
            String localVersion = privateMines.getDescription().getVersion();
            context.getSource().getSender().sendRichMessage("<green>Private Mines is running v" + localVersion);
            return SINGLE_SUCCESS;
        }).build();
    }

    private CommandNode<CommandSourceStack> giveLogic() {
        return literal("give").requires(commandSourceStack -> commandSourceStack.getSender()
                        .hasPermission("privatemines.give"))
                .then(argument("player", ArgumentTypes.player()).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender commandSender = source.getSender();
                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(source)
                            .getFirst();

                    if (target == null || !target.isOnline()) {
                        commandSender.sendRichMessage("<red>The specified player couldn't be found!");
                        return SINGLE_SUCCESS;
                    }

                    Location location = mineWorldManager.getNextFreeLocation();
                    if (!mineTypeRegistry.isDefaultMineSet()) {
                        commandSender.sendRichMessage("<red>Default mine type is not set!");
                        return SINGLE_SUCCESS;
                    }

                    MineType defaultMineType = mineTypeRegistry.getDefaultMineType();

                    UUID targetUniqueId = target.getUniqueId();
                    if (mineService.has(targetUniqueId)) {
                        commandSender.sendRichMessage("<red>The specified player already owns a mine!");
                        return SINGLE_SUCCESS;
                    }

                    mineFactory.create(targetUniqueId, location, defaultMineType);
                    commandSender.sendRichMessage("<green>Gave " + target.getName() + " a mine!");
                    return SINGLE_SUCCESS;
                }))
                .build();
    }

    private CommandNode<CommandSourceStack> deleteMineLogic() {
        return literal("delete").requires(commandSourceStack -> commandSourceStack.getSender()
                        .hasPermission("privatemines.delete"))
                .then(argument("player", ArgumentTypes.player()).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender commandSender = source.getSender();
                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(source)
                            .getFirst();

                    if (target == null || !target.isOnline()) {
                        commandSender.sendRichMessage("<red>The specified player couldn't be found!");
                        return SINGLE_SUCCESS;
                    }

                    if (!mineService.has(target.getUniqueId())) {
                        commandSender.sendRichMessage("<red>The specified player does not own a mine!");
                        return SINGLE_SUCCESS;
                    }

                    Mine mine = mineService.get(target.getUniqueId());
                    mine.stopTasks();
                    mineService.delete(mine);
//                    SQLUtils.delete(mine);

                    commandSender.sendRichMessage("<green>Successfully deleted mine of player " + target.getName());
                    return SINGLE_SUCCESS;
                }))
                .build();
    }

    private CommandNode<CommandSourceStack> upgradeMineLogic() {
        return literal("upgrade").requires(commandSourceStack -> {
            CommandSender sender = commandSourceStack.getSender();
            return sender.hasPermission("privatemines.upgrade") && sender instanceof Player;
        }).then(argument("times", IntegerArgumentType.integer(1)).executes(context -> {
            Player player = (Player) context.getSource().getSender();
            int times = context.getArgument("times", Integer.class);

            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());

            for (int i = 0; i < times; i++) {
                MineData mineData = mine.getMineData();

                MineType currentType = mineData.getMineType();
                MineType nextType = mineTypeRegistry.getNextMineType(currentType);

                if (currentType == nextType) {
                    player.sendRichMessage("<green>Your mine is already at maxMineCorner level!");
                    return SINGLE_SUCCESS;
                }

                double cost = nextType.upgradeCost();
                if (!economy.has(player, cost)) {
                    player.sendRichMessage("<red>You cannot afford this upgrade!");
                    return SINGLE_SUCCESS;
                }

                economy.withdrawPlayer(player, cost);
                mineService.upgrade(mine);
                mineService.handleReset(mine);
                player.sendRichMessage("<green>Mine upgraded successfully!");
            }

            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> forceUpgradeMineLogic() {
        return literal("forceupgrade").requires(commandSourceStack -> {
            return commandSourceStack.getSender().hasPermission("privatemines.forceupgrade");
        }).then(argument("player", ArgumentTypes.player()).executes(context -> {
            CommandSourceStack source = context.getSource();
            CommandSender sender = source.getSender();
            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                    .resolve(source)
                    .getFirst();
            if (!mineService.has(target.getUniqueId())) {
                sender.sendRichMessage("<red>This player does not have a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(target.getUniqueId());

            // todo: seems like this is used more than 2 times so its best to make a method.
            mineService.upgrade(mine);
            mineService.reset(mine);

            target.sendRichMessage("<green>Your mine has been upgraded!");

            MineData mineData = mine.getMineData();
            MineStructure mineStructure = mineData.getMineStructure();

            BoundingBox schematicArea = mineStructure.schematicBoundingBox();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (schematicArea.contains(onlinePlayer.getLocation().toVector()) && mineWorldManager.getMinesWorld()
                        .equals(onlinePlayer.getWorld())) {
                    Bukkit.dispatchCommand(onlinePlayer, "spawn"); //todo: change this since command may not exist.
                }
            }

            // todo: fix sql

//            SQL_HELPER.executeUpdate(
//                    "INSERT INTO privatemines (owner, mineType, mineLocation, corner1, corner2, fullRegionMin, fullRegionMax, spawn, tax, isOpen, maxPlayers, maxMineSize, materialChance) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
//                    String.valueOf(mineData.getMineOwner()),
//                    mineData.getMineType(),
//                    LocationUtils.toString(mineData.getMineLocation()),
//                    LocationUtils.toString(mineData.getMinimumMining()),
//                    LocationUtils.toString(mineData.getMaximumMining()),
//                    LocationUtils.toString(mineData.getMinimumFullRegion()),
//                    LocationUtils.toString(mineData.getMaximumFullRegion()),
//                    LocationUtils.toString(mineData.getSpawnLocation()),
//                    mineData.getTax(),
//                    mineData.isOpen(),
//                    mineData.getMaxPlayers(),
//                    mineData.getMaxMineSize(),
//                    mineData.getMaterials()
//            );

            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> forceResetMineLogic() {
        return literal("forcereset").requires(commandSourceStack -> {
            return commandSourceStack.getSender().hasPermission("privatemines.forcereset");
        }).then(argument("player", ArgumentTypes.player()).executes(context -> {
            CommandSourceStack source = context.getSource();
            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                    .resolve(source)
                    .getFirst();
            if (!mineService.has(target.getUniqueId())) {
                target.sendRichMessage("<red>This player does not have a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(target.getUniqueId());
            mineService.reset(mine);
            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> resetMineLogic() {
        return literal("reset").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.reset") && commandSender instanceof Player;
        }).executes(context -> {
            Player sender = (Player) context.getSource().getSender();
            UUID uuid = sender.getUniqueId();

            if (mineService.has(uuid)) {
                sender.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(uuid);


            if (resetCooldown == 0) {
                mineService.handleReset(mine);
                return SINGLE_SUCCESS;
            }

            if (!Cooldowns.hasExpiredCooldown(uuid)) {
                int timeLeft = Cooldowns.get(uuid);
                sender.sendRichMessage("<red>Please wait " + timeLeft + " seconds to reset your mine again!");
                return SINGLE_SUCCESS;
            }

            Cooldowns.set(uuid, resetCooldown);

            mineService.handleReset(mine);
            return SINGLE_SUCCESS;
        }).build();
    }

    private CommandNode<CommandSourceStack> teleportMineLogic() {
        return literal("teleport").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.teleport") && commandSender instanceof Player;
        }).executes(context -> {
            Player player = (Player) context.getSource().getSender();
            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());
            mine.teleport(player);
            return SINGLE_SUCCESS;
        }).build();
    }

    private CommandNode<CommandSourceStack> goMineLogic() {
        return literal("go").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.go") && commandSender instanceof Player;
        }).then(argument("player", ArgumentTypes.player()).executes(context -> {
            CommandSourceStack source = context.getSource();
            Player player = (Player) source.getSender();
            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                    .resolve(source)
                    .getFirst();

            if (!mineService.has(target.getUniqueId())) {
                player.sendRichMessage("<red>This player does not have a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(target.getUniqueId());
            MineData mineData = mine.getMineData();

            if (!mineData.isOpen()) {
                player.sendRichMessage("<red>This mine is closed!");
                return SINGLE_SUCCESS;
            }

            if (mineData.isBanned(player.getUniqueId())) {
                player.sendRichMessage("<red>You are banned from this mine!");
                return SINGLE_SUCCESS;
            }

            mine.teleport(player);
            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> expandMineLogic() {
        return literal("expand").requires(commandSourceStack -> {
                    return commandSourceStack.getSender().hasPermission("privatemines.expand");
                })
                .then(argument("player", ArgumentTypes.player()).then(argument(
                        "amount",
                        IntegerArgumentType.integer(1)
                ).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender commandSender = source.getSender();
                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(source)
                            .getFirst();
                    int amount = context.getArgument("amount", Integer.class);

                    if (!mineService.has(target.getUniqueId())) {
                        commandSender.sendRichMessage("<red>This player does not have a mine!");
                        return SINGLE_SUCCESS;
                    }

                    Mine mine = mineService.get(target.getUniqueId());
                    if (!mineService.canExpand(mine, amount)) {
                        commandSender.sendRichMessage("<red>Cannot expand mine by that amount!");
                        return SINGLE_SUCCESS;
                    }

                    mineService.expand(mine, amount);
                    commandSender.sendRichMessage("<green>Successfully expanded " + target.getName() + "'s mine!");
                    return SINGLE_SUCCESS;
                })))
                .build();

    }

    private CommandNode<CommandSourceStack> openCloseMineLogic(String literal, boolean state) {
        return literal(literal).requires(commandSourceStack -> {
            return commandSourceStack.getSender().hasPermission("privatemines." + literal);
        }).executes(context -> {
            Player player = (Player) context.getSource().getSender();

            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());
            MineData mineData = mine.getMineData();
            mineData.setOpen(state);

            String message = state ? "opened" : "closed";
//            SQLUtils.update(mine);
            player.sendRichMessage("<green>Mine " + message + " successfully!");
            return SINGLE_SUCCESS;
        }).build();
    }

    private CommandNode<CommandSourceStack> banFromMineLogic() {
        return literal("ban").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.ban") && commandSender instanceof Player;
        }).then(argument("player", ArgumentTypes.player()).executes(context -> {
            CommandSourceStack source = context.getSource();
            Player player = (Player) source.getSender();
            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                    .resolve(source)
                    .getFirst();

            UUID playerUniqueId = player.getUniqueId();
            if (!mineService.has(playerUniqueId)) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(playerUniqueId);
            UUID targetUniqueId = target.getUniqueId();

            if (mine.isBanned(targetUniqueId)) {
                player.sendRichMessage("<red>This player is already banned from your mine!");
                return SINGLE_SUCCESS;
            }

            mine.ban(targetUniqueId);
            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> unbanFromMineLogic() {
        return literal("unban").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.unban") && commandSender instanceof Player;
        }).then(argument("player", ArgumentTypes.player()).executes(context -> {
            CommandSourceStack source = context.getSource();
            Player player = (Player) source.getSender();
            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                    .resolve(source)
                    .getFirst();

            UUID playerUniqueId = player.getUniqueId();
            if (!mineService.has(playerUniqueId)) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(playerUniqueId);
            if (!mine.isBanned(target.getUniqueId())) {
                player.sendRichMessage("<red>This player is not banned from your mine!");
                return SINGLE_SUCCESS;
            }

            mine.unban(target.getUniqueId());
            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> taxLogic() {
        return literal("tax").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.tax") && commandSender instanceof Player;
        }).then(argument("tax", IntegerArgumentType.integer(0, 100)).executes(context -> {
            Player player = (Player) context.getSource().getSender();
            double tax = context.getArgument("tax", Double.class);

            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());
            MineData mineData = mine.getMineData();
            mineData.setTax(tax);
//            SQLUtils.update(mine);

            player.sendRichMessage("<green>Successfully updated tax to " + tax + "%");
            return SINGLE_SUCCESS;
        })).build();
    }

    private CommandNode<CommandSourceStack> setBlocksLogic() {
        return literal("setblocks").requires(commandSourceStack -> {
                    return commandSourceStack.getSender().hasPermission("privatemines.setblocks");
                })
                .then(argument("player", ArgumentTypes.player()).then(argument(
                        "material",
                        ArgumentTypes.namespacedKey()
                ).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    Player player = (Player) source.getSender();
                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(source)
                            .getFirst();

                    NamespacedKey material = context.getArgument("material", NamespacedKey.class);
                    Material registryMaterial = Registry.MATERIAL.get(material);

                    if (registryMaterial == null) {
                        player.sendRichMessage("<red>Failed to find Material: " + material);
                        return SINGLE_SUCCESS;
                    }

                    if (!mineService.has(target.getUniqueId())) {
                        player.sendRichMessage("<red>This player does not have a mine!");
                        return SINGLE_SUCCESS;
                    }

                    Mine mine = mineService.get(target.getUniqueId());
                    MineData mineData = mine.getMineData();
                    mineData.getMaterials().add(1, registryMaterial);

//                    SQLUtils.update(mine);

                    player.sendRichMessage("<green>Successfully updated materialChance!");
                    return SINGLE_SUCCESS;
                })))
                .build();
    }


//    @Subcommand("claim")
//    @CommandPermission("privatemines.claim")
//    public void claim(Player player) {
//        if (mineService.hasMine(player)) {
//            audienceUtils.sendMessage(player, MessagesConfig.playerAlreadyOwnsAMine);
//        } else {
//            String mineRegionName = String.format("mine-%s", player.getUniqueId());
//            String fullRegionName = String.format("full-mine-%s", player.getUniqueId());
//
//            PregenStorage pregenStorage = privateMines.getPregenStorage();
//            if (pregenStorage.isAllRedeemed()) {
////        player.sendMessage(ChatColor.RED + "All the mines have been claimed...");
//                audienceUtils.sendMessage(player, MessagesConfig.allMinesClaimed);
//            } else {
//                PregenMine pregenMine = pregenStorage.getAndRemove();
//                MineType mineType = mineTypeManager.getDefault();
//                Location location = pregenMine.getLocation();
//                Location spawn = pregenMine.getSpawnLocation();
//                Location corner1 = pregenMine.getLowerRails();
//                Location corner2 = pregenMine.getUpperRails();
//                Location minimum = pregenMine.getFullMin();
//                Location maximum = pregenMine.getFullMax();
//                BlockVector3 miningRegionMin = BukkitAdapter.asBlockVector(Objects.requireNonNull(corner1));
//                BlockVector3 miningRegionMax = BukkitAdapter.asBlockVector(Objects.requireNonNull(corner2));
//                BlockVector3 fullRegionMin = BukkitAdapter.asBlockVector(Objects.requireNonNull(minimum));
//                BlockVector3 fullRegionMax = BukkitAdapter.asBlockVector(Objects.requireNonNull(maximum));
//
//                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//                RegionManager regionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(spawn)
//                        .getWorld()));
//
//                ProtectedCuboidRegion miningRegion = new ProtectedCuboidRegion(
//                        mineRegionName,
//                        miningRegionMin,
//                        miningRegionMax
//                );
//                ProtectedCuboidRegion fullRegion = new ProtectedCuboidRegion(
//                        fullRegionName,
//                        fullRegionMin,
//                        fullRegionMax
//                );
//
//                if (regionManager != null) {
//                    regionManager.addRegion(miningRegion);
//                    regionManager.addRegion(fullRegion);
//                }
//
//                Mine mine = new Mine(privateMines);
//                MineData mineData = new MineData(
//                        player.getUniqueId(),
//                        corner2,
//                        corner1,
//                        minimum,
//                        maximum,
//                        Objects.requireNonNull(location),
//                        spawn,
//                        mineType,
//                        false,
//                        5.0
//                );
//                mine.setMineData(mineData);
//                SQLUtils.claim(location);
//                SQLUtils.insert(mine);
//
//                mineService.addMine(player.getUniqueId(), mine);
//
//                Task.syncDelayed(() -> spawn.getBlock().setType(Material.AIR, false));
//                pregenMine.teleport(player);
//                mine.handleReset();
//            }
//        }
//    }
}
