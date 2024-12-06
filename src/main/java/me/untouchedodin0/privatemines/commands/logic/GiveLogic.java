package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.template.MineTemplateRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

@RequiredArgsConstructor
public class GiveLogic implements CommandLogic {
    private final MineService mineService;
    private final MineTemplateRegistry mineTemplateRegistry;

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name("give").requires(commandSourceStack -> commandSourceStack.getSender()
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
}
