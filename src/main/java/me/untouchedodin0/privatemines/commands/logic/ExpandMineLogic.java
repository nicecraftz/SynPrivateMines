package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ExpandMineLogic extends CommandLogic {

    public ExpandMineLogic(MineService mineService) {
        super(mineService);
    }

    @Override
    public String name() {
        return "expand";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {

        return literal(name()).requires(hasPermission(name())).then(argument("player", ArgumentTypes.player()).then(argument(
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
        }))).build();
    }
}
