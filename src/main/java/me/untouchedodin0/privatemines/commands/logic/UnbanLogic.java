package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

@RequiredArgsConstructor
public class UnbanLogic implements CommandLogic {
    private final MineService mineService;

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name("unban").requires(commandSourceStack -> {
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
}
