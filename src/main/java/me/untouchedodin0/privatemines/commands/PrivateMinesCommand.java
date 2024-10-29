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
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.Entry;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.template.MineTemplateRegistry;
import me.untouchedodin0.privatemines.utils.Cooldowns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


public class PrivateMinesCommand {
    private final PrivateMines privateMines;
    private final MineService mineService;
    private final MineTemplateRegistry mineTemplateRegistry;
    private final Economy economy;

    @Entry(key = "reset-cooldown", section = "mine", value = "15", type = ConfigurationValueType.INT)
    private int resetCooldown;

    public PrivateMinesCommand(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.economy = privateMines.getEconomy();
        this.mineService = privateMines.getMineService();
        this.mineTemplateRegistry = privateMines.getMineTemplateRegistry();
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

                    if (!mineTemplateRegistry.hasDefaultTemplate()) {
                        commandSender.sendRichMessage("<red>Default mine type is not set!");
                        return SINGLE_SUCCESS;
                    }

                    UUID targetUniqueId = target.getUniqueId();
                    if (mineService.has(targetUniqueId)) {
                        commandSender.sendRichMessage("<red>The specified player already owns a mine!");
                        return SINGLE_SUCCESS;
                    }

                    mineService.create(targetUniqueId);
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
                    mineService.delete(mine);

                    commandSender.sendRichMessage("<green>Successfully deleted mine of player " + target.getName());
                    return SINGLE_SUCCESS;
                }))
                .build();
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

            if (!mineService.has(uuid)) {
                sender.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(uuid);

            if (resetCooldown == 0) {
                mineService.reset(mine);
                return SINGLE_SUCCESS;
            }

            if (!Cooldowns.hasExpiredCooldown(uuid)) {
                int timeLeft = Cooldowns.get(uuid);
                sender.sendRichMessage("<red>Please wait " + timeLeft + " seconds to reset your mine again!");
                return SINGLE_SUCCESS;
            }

            Cooldowns.set(uuid, resetCooldown);

            mineService.reset(mine);
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

            if (!mine.isOpen()) {
                player.sendRichMessage("<red>This mine is closed!");
                return SINGLE_SUCCESS;
            }

            if (mine.isBanned(player.getUniqueId())) {
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
            mine.setOpen(state);

            String message = state ? "opened" : "closed";
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
            mine.setTax(tax);
            player.sendRichMessage("<green>Successfully updated tax to " + tax + "%");
            return SINGLE_SUCCESS;
        })).build();
    }
}