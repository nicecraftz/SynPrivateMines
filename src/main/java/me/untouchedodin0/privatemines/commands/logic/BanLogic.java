package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class BanLogic extends CommandLogic {

    public BanLogic(MineService mineService) {
        super(mineService);
    }

    @Override
    public String name() {
        return "ban";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return literal(name()).requires(isPlayerHasPermissionAndHasMine(name()))
                .then(argument("player", ArgumentTypes.player()).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    Player player = (Player) source.getSender();
                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(source)
                            .getFirst();

                    UUID playerUniqueId = player.getUniqueId();
                    Mine mine = mineService.get(playerUniqueId);
                    UUID targetUniqueId = target.getUniqueId();

                    if (mine.isBanned(targetUniqueId)) {
                        player.sendRichMessage("<red>This player is already banned from your mine!");
                        return SINGLE_SUCCESS;
                    }

                    mine.ban(targetUniqueId);
                    return SINGLE_SUCCESS;
                }))
                .build();
    }
}
