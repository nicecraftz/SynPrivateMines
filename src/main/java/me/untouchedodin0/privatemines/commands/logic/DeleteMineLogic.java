package me.untouchedodin0.privatemines.commands.logic;

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

public class DeleteMineLogic extends CommandLogic {

    public DeleteMineLogic(MineService mineService) {
        super(mineService);
    }

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return literal(name()).requires(isPlayerAndHasPermission(name()))
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
}
