package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GoMineLogic extends CommandLogic {

    @Override
    public String name() {
        return "go";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return literal(name()).requires(commandSourceStack -> {
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

            if (!mine.getMineSettings().isOpen()) {
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
}
