package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

public class ForceResetMineLogic extends CommandLogic {

    public ForceResetMineLogic(MineService mineService) {
        super(mineService);
    }

    @Override
    public String name() {
        return "forcereset";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name("forcereset").requires(commandSourceStack -> {
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
}
